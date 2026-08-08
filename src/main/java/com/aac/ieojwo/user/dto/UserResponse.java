package com.aac.ieojwo.user.dto;

import com.aac.ieojwo.user.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponse(Long id, String name, UserMode mode, GridSize gridSize, boolean active,
                           LocalDate birthDate, String emergencyContact, String notes, String profileImageUrl,
                           VoiceType voiceType, BigDecimal speechRate, AacUserSetupStep setupStep,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static UserResponse from(AacUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getMode(), user.getGridSize(), user.isActive(),
                user.getBirthDate(), user.getEmergencyContact(), user.getNotes(), user.getProfileImageUrl(),
                user.getVoiceType(), user.getSpeechRate(), user.getSetupStep(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
