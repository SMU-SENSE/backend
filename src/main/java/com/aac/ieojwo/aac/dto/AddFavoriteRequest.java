package com.aac.ieojwo.aac.dto;

import jakarta.validation.constraints.NotNull;

public record AddFavoriteRequest(
        @NotNull(message = "상징 ID는 필수입니다.")
        Long symbolId
) {
}
