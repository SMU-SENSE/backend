package com.aac.ieojwo.guardian.dto;

import com.aac.ieojwo.guardian.domain.GuardianRole;
import jakarta.validation.constraints.NotNull;

public record LinkGuardianRequest(
        @NotNull(message = "보호자 ID는 필수입니다.")
        Long guardianId,

        @NotNull(message = "보호자 역할은 필수입니다.")
        GuardianRole role,

        boolean primaryGuardian
) {
}
