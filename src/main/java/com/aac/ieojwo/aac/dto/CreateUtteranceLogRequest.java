package com.aac.ieojwo.aac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @param converted "문장으로 변환하기"(LLM)를 거쳤는지 여부. 변환된 문장 자체는 저장하지 않는다.
 * @param spoken    TTS로 실제 읽었는지 여부
 */
public record CreateUtteranceLogRequest(
        @NotEmpty(message = "카드 목록은 필수입니다.")
        @Size(max = 30, message = "카드는 30장 이하여야 합니다.")
        List<@NotBlank(message = "카드 ID는 비어 있을 수 없습니다.") String> cardCodes,

        boolean converted,

        boolean spoken,

        LocalDateTime occurredAt
) {
}
