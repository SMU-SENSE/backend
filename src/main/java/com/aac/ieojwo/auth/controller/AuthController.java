package com.aac.ieojwo.auth.controller;

import com.aac.ieojwo.auth.dto.AccountResponse;
import com.aac.ieojwo.auth.dto.OnboardingRequest;
import com.aac.ieojwo.auth.service.AuthService;
import com.aac.ieojwo.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public ApiResponse<AccountResponse> me(@AuthenticationPrincipal OidcUser principal) {
        return ApiResponse.ok(authService.getCurrentAccount(principal));
    }

    @PostMapping("/onboarding")
    public ApiResponse<AccountResponse> onboarding(
            @AuthenticationPrincipal OidcUser principal,
            @Valid @RequestBody OnboardingRequest request
    ) {
        return ApiResponse.ok(
                authService.completeOnboarding(principal, request),
                "최초 설정이 완료되었습니다."
        );
    }
}
