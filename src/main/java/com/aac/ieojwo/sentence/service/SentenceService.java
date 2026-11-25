package com.aac.ieojwo.sentence.service;

import com.aac.ieojwo.sentence.domain.SentenceStyle;
import com.aac.ieojwo.sentence.dto.GenerateSentenceRequest;
import com.aac.ieojwo.sentence.dto.SentenceResponse;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.symbol.service.SymbolService;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.service.AacUserAccessService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * "문장으로 변환하기": 단어 카드 순서를 LLM으로 자연스러운 문장 하나로 바꾼다.
 * 결과는 저장하지 않는다(사용 기록은 utterance-logs 로 따로 남김). LLM 이 실패하면 카드 라벨을 이어서 돌려준다.
 * 의도적으로 @Transactional 을 붙이지 않는다 — LLM 응답을 기다리는 동안 DB 커넥션을 잡고 있지 않도록.
 */
@Service
public class SentenceService {

    private static final int MAX_SENTENCE_LENGTH = 200;

    private final AacUserAccessService userAccessService;
    private final SymbolService symbolService;
    private final OpenAiClient openAiClient;

    public SentenceService(AacUserAccessService userAccessService, SymbolService symbolService,
                           OpenAiClient openAiClient) {
        this.userAccessService = userAccessService;
        this.symbolService = symbolService;
        this.openAiClient = openAiClient;
    }

    public SentenceResponse generate(OidcUser principal, Long userId, GenerateSentenceRequest request) {
        AacUser user = userAccessService.requireAccessibleUser(principal, userId);
        List<String> labels = symbolService.getUsableCards(request.cardCodes(), user).stream()
                .map(Symbol::getName)
                .toList();
        SentenceStyle style = request.style() == null ? SentenceStyle.POLITE : request.style();
        String cards = labels.stream().map(label -> "[" + label + "]").collect(Collectors.joining(" "));

        return openAiClient.complete(systemPrompt(style), cards)
                .map(SentenceService::cleanUp)
                .filter(sentence -> !sentence.isEmpty() && sentence.length() <= MAX_SENTENCE_LENGTH)
                .map(sentence -> new SentenceResponse(sentence, true))
                .orElseGet(() -> new SentenceResponse(String.join(" ", labels), false));
    }

    static String systemPrompt(SentenceStyle style) {
        String tone = style == SentenceStyle.CASUAL ? "반말" : "존댓말";
        return """
                너는 AAC(보완대체의사소통) 앱의 문장 도우미다. 발달장애가 있는 사용자가 고른 단어 카드들을 \
                그 사람이 하려는 말로, 자연스러운 한국어 구어체 문장 하나로 바꾼다.
                규칙:
                - 카드에 없는 내용을 지어내지 않는다. 조사, 어미, 연결어만 채운다.
                - 자연스럽다면 카드 순서를 바꿔도 된다.
                - '싫어요', '아니' 같은 부정 카드의 뜻을 반드시 그대로 살린다.
                - %s로 쓴다.
                - 짧은 문장 하나만 답한다. 따옴표, 설명, 다른 후보를 붙이지 않는다.
                예: [밥] [먹다] [싫어요] → 밥 먹기 싫어요
                """.formatted(tone);
    }

    /** 모델이 따옴표나 여러 줄을 붙여 답해도 첫 문장만 쓴다. */
    static String cleanUp(String text) {
        String firstLine = text.strip().lines().findFirst().orElse("").strip();
        return firstLine.replaceAll("^[\"'“”‘’]+|[\"'“”‘’]+$", "").strip();
    }
}
