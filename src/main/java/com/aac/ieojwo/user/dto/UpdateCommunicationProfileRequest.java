package com.aac.ieojwo.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "의사소통 프로필 부분 수정 요청. 생략한 값은 유지됩니다.")
public record UpdateCommunicationProfileRequest(
        @Min(1) @Max(4) Integer sentenceLevel,
        @Min(1) @Max(30) Integer maxRecommendedSentenceWords,
        Boolean easyWordsPreferred,
        Boolean abstractExpressionsRestricted,
        Boolean complexGrammarRestricted,
        Boolean conciseDirectPreferred
) {
}
