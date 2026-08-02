package com.aac.ieojwo.symbol.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSymbolRequest(
        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @NotBlank(message = "상징 이름은 필수입니다.")
        @Size(max = 80, message = "상징 이름은 80자 이하여야 합니다.")
        String name,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
        String imageUrl,

        @NotBlank(message = "TTS 문구는 필수입니다.")
        @Size(max = 200, message = "TTS 문구는 200자 이하여야 합니다.")
        String ttsText,

        boolean emergency,

        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        int displayOrder
) {
}
