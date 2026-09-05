package com.aac.ieojwo.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterPushTokenRequest(
        @NotBlank(message = "푸시 토큰은 필수입니다.") String token
) {
}
