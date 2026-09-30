package com.aac.ieojwo.ai.service;

import com.aac.ieojwo.ai.dto.PersonalizedAacContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SentenceRecommendationPromptBuilder {
    private static final String SYSTEM_INSTRUCTION = """
            You generate one AAC sentence for the specified user.
            Use the personalized context and its vocabulary priority.
            Return exactly one plain sentence only.
            Never return alternatives, explanations, Markdown, bullets, numbering, or JSON.
            Respect the maximum recommended sentence length and simplification settings.
            """;

    private final ObjectMapper objectMapper;

    public SentenceRecommendationPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RecommendationPrompt build(PersonalizedAacContext context) {
        try {
            return new RecommendationPrompt(SYSTEM_INSTRUCTION.strip(),
                    objectMapper.writeValueAsString(context), context);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("개인화 AAC 컨텍스트 직렬화에 실패했습니다.", exception);
        }
    }

    public record RecommendationPrompt(String systemInstruction, String contextJson,
                                       PersonalizedAacContext context) {
    }
}
