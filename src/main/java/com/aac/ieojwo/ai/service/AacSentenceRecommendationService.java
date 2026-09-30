package com.aac.ieojwo.ai.service;

import com.aac.ieojwo.ai.dto.PersonalizedAacContext;
import com.aac.ieojwo.ai.dto.SentenceRecommendationResponse;
import com.aac.ieojwo.ai.spi.AacSentenceRecommendationProvider;
import com.aac.ieojwo.common.exception.BadGatewayException;
import com.aac.ieojwo.common.exception.ServiceUnavailableException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AacSentenceRecommendationService {
    private static final int MAX_PROVIDER_RESPONSE_LENGTH = 500;

    private final PersonalizedAacContextBuilder contextBuilder;
    private final SentenceRecommendationPromptBuilder promptBuilder;
    private final List<AacSentenceRecommendationProvider> providers;

    public AacSentenceRecommendationService(PersonalizedAacContextBuilder contextBuilder,
                                            SentenceRecommendationPromptBuilder promptBuilder,
                                            List<AacSentenceRecommendationProvider> providers) {
        this.contextBuilder = contextBuilder;
        this.promptBuilder = promptBuilder;
        this.providers = providers;
    }

    public SentenceRecommendationResponse recommend(OidcUser principal, Long userId,
                                                     String currentSituation) {
        PersonalizedAacContext context = contextBuilder.build(principal, userId, currentSituation);
        if (providers.isEmpty()) {
            throw new ServiceUnavailableException("AI 문장 추천 제공자가 설정되지 않았습니다.");
        }
        String raw = providers.getFirst().recommend(promptBuilder.build(context));
        return new SentenceRecommendationResponse(validate(raw));
    }

    private String validate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadGatewayException("AI 문장 추천 결과가 비어 있습니다.");
        }
        String sentence = raw.trim();
        if (sentence.length() > MAX_PROVIDER_RESPONSE_LENGTH || sentence.contains("\n")
                || sentence.startsWith("-") || sentence.startsWith("*")
                || sentence.matches("^\\d+[.)].*")) {
            throw new BadGatewayException("AI 제공자가 단일 문장 응답 계약을 지키지 않았습니다.");
        }
        return sentence;
    }
}
