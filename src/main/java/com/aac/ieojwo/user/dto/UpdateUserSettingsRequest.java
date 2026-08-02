package com.aac.ieojwo.user.dto;

import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserMode;
import jakarta.validation.constraints.NotNull;

public record UpdateUserSettingsRequest(
        @NotNull(message = "사용자 모드는 필수입니다.")
        UserMode mode,

        @NotNull(message = "격자 크기는 필수입니다.")
        GridSize gridSize
) {
}
