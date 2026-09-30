package com.aac.ieojwo.ai.spi;

import com.aac.ieojwo.ai.service.SentenceRecommendationPromptBuilder.RecommendationPrompt;

public interface AacSentenceRecommendationProvider {
    String recommend(RecommendationPrompt prompt);
}
