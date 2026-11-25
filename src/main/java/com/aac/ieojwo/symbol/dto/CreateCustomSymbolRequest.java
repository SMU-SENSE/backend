package com.aac.ieojwo.symbol.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCustomSymbolRequest(
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "카드 이름은 필수입니다.")
        @Size(max = 80, message = "카드 이름은 80자 이하여야 합니다.")
        String name,

        @Size(max = 200, message = "TTS 문구는 200자 이하여야 합니다.")
        String ttsText,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl
) {
}
