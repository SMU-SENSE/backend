package com.aac.ieojwo.sentence.dto;

import com.aac.ieojwo.sentence.domain.SentenceStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * @param cardCodes 고른 카드 ID 순서 (model.json 과 같은 card_code)
 * @param style     없으면 POLITE(존댓말)
 */
public record GenerateSentenceRequest(
        @NotEmpty(message = "카드 목록은 필수입니다.")
        @Size(max = 30, message = "카드는 30장 이하여야 합니다.")
        List<@NotBlank(message = "카드 ID는 비어 있을 수 없습니다.") String> cardCodes,

        SentenceStyle style
) {
}
