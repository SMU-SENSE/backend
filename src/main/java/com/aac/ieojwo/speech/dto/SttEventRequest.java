package com.aac.ieojwo.speech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "프론트 또는 외부 STT가 생성한 텍스트 결과. 원본 음성은 받지 않습니다.")
public record SttEventRequest(
        @Size(max = 2000) String recognizedText,
        boolean successful,
        @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
        Instant occurredAt
) {
    @AssertTrue(message = "성공한 STT 결과에는 recognizedText가 필요합니다.")
    public boolean isSuccessfulResultValid() {
        return !successful || (recognizedText != null && !recognizedText.isBlank());
    }
}
