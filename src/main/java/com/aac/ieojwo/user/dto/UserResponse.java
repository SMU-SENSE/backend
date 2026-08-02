package com.aac.ieojwo.user.dto;

import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserMode;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        UserMode mode,
        GridSize gridSize,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(AacUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getMode(),
                user.getGridSize(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
