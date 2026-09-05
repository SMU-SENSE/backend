package com.aac.ieojwo.notification.dto;

import com.aac.ieojwo.notification.domain.GuardianNotification;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        Long aacUserId,
        String aacUserName,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(GuardianNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getAacUser().getId(),
                notification.getAacUser().getName(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
