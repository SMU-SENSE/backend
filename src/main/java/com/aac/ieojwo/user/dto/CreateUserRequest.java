package com.aac.ieojwo.user.dto;

import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Schema(description = "AAC 사용자 이름", example = "박지현")
        @NotBlank(message = "사용자 이름은 필수입니다.")
        @Size(max = 50, message = "사용자 이름은 50자 이하여야 합니다.")
        String name,

        @Schema(description = "화면 모드", example = "SIMPLE")
        @NotNull(message = "사용자 모드는 필수입니다.")
        UserMode mode,

        @Schema(description = "AAC 격자 크기", example = "GRID_2X2")
        @NotNull(message = "격자 크기는 필수입니다.")
        GridSize gridSize
) {
}
