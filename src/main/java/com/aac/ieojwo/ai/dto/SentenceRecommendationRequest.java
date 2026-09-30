package com.aac.ieojwo.ai.dto;

import jakarta.validation.constraints.Size;

public record SentenceRecommendationRequest(
        @Size(max = 500) String currentSituation
) {
}
