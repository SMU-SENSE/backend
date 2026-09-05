package com.aac.ieojwo.notification.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.notification.dto.NotificationResponse;
import com.aac.ieojwo.notification.dto.RegisterPushTokenRequest;
import com.aac.ieojwo.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public ApiResponse<List<NotificationResponse>> findAll(@AuthenticationPrincipal OidcUser principal) {
        return ApiResponse.ok(notificationService.findMyNotifications(principal));
    }

    @PatchMapping("/notifications/{id}/read")
    public ApiResponse<Void> markRead(@AuthenticationPrincipal OidcUser principal, @PathVariable Long id) {
        notificationService.markRead(principal, id);
        return ApiResponse.ok(null, "확인 처리되었습니다.");
    }

    @PostMapping("/push-token")
    public ApiResponse<Void> registerPushToken(@AuthenticationPrincipal OidcUser principal,
                                               @Valid @RequestBody RegisterPushTokenRequest request) {
        notificationService.registerPushToken(principal, request.token());
        return ApiResponse.ok(null, "푸시 토큰이 등록되었습니다.");
    }
}
