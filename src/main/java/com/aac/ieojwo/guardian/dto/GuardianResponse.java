package com.aac.ieojwo.guardian.dto;

import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.domain.GuardianRole;
import com.aac.ieojwo.guardian.domain.UserGuardian;

public record GuardianResponse(
        Long id,
        String name,
        String email,
        String phone,
        GuardianRole role,
        boolean primaryGuardian
) {
    public static GuardianResponse from(Guardian guardian) {
        return new GuardianResponse(
                guardian.getId(), guardian.getName(), guardian.getEmail(), guardian.getPhone(), null, false
        );
    }

    public static GuardianResponse from(UserGuardian relation) {
        Guardian guardian = relation.getGuardian();
        return new GuardianResponse(
                guardian.getId(),
                guardian.getName(),
                guardian.getEmail(),
                guardian.getPhone(),
                relation.getRole(),
                relation.isPrimaryGuardian()
        );
    }
}
