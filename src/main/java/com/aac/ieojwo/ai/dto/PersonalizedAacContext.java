package com.aac.ieojwo.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "외부 LLM에 전달할 수 있는 사용자 개인화 AAC 컨텍스트")
public record PersonalizedAacContext(
        Long aacUserId,
        Integer age,
        String profileNotes,
        CommunicationPreferences communicationPreferences,
        List<VocabularyItem> prioritizedVocabulary,
        List<String> recentExpressions,
        String currentSituation
) {
    public record CommunicationPreferences(
            int sentenceLevel,
            int maxRecommendedSentenceWords,
            boolean easyWordsPreferred,
            boolean abstractExpressionsRestricted,
            boolean complexGrammarRestricted,
            boolean conciseDirectPreferred
    ) {
    }

    public record VocabularyItem(
            Long cardId,
            String displayText,
            String ttsText,
            VocabularyPriority priority,
            boolean important,
            boolean favorite,
            long usageCount,
            Instant lastUsedAt
    ) {
    }
}
