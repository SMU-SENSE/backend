package com.aac.ieojwo.user.controller;

import com.aac.ieojwo.aac.dto.AddFavoriteRequest;
import com.aac.ieojwo.aac.dto.CreateUsageLogRequest;
import com.aac.ieojwo.aac.dto.UsageLogResponse;
import com.aac.ieojwo.aac.service.AacUsageService;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.guardian.dto.GuardianResponse;
import com.aac.ieojwo.guardian.dto.LinkGuardianRequest;
import com.aac.ieojwo.guardian.service.GuardianService;
import com.aac.ieojwo.symbol.dto.SymbolResponse;
import com.aac.ieojwo.user.dto.CreateUserRequest;
import com.aac.ieojwo.user.dto.UpdateUserSettingsRequest;
import com.aac.ieojwo.user.dto.UpdateGridRequest;
import com.aac.ieojwo.user.dto.UpdateVoiceSettingsRequest;
import com.aac.ieojwo.user.dto.OnboardingSummaryResponse;
import com.aac.ieojwo.user.dto.UserResponse;
import com.aac.ieojwo.user.service.UserService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/me/aac-users")
public class MeAacUserController {

    private final UserService userService;
    private final AacUsageService aacUsageService;
    private final GuardianService guardianService;

    public MeAacUserController(UserService userService, AacUsageService aacUsageService,
                               GuardianService guardianService) {
        this.userService = userService;
        this.aacUsageService = aacUsageService;
        this.guardianService = guardianService;
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll(@AuthenticationPrincipal OidcUser principal) {
        return ApiResponse.ok(userService.findAll(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@AuthenticationPrincipal OidcUser principal,
                                            @Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.create(principal, request), "사용자가 생성되었습니다.");
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> findById(@AuthenticationPrincipal OidcUser principal,
                                              @PathVariable Long userId) {
        return ApiResponse.ok(userService.findById(principal, userId));
    }

    @PatchMapping("/{userId}/settings")
    public ApiResponse<UserResponse> updateSettings(@AuthenticationPrincipal OidcUser principal,
                                                    @PathVariable Long userId,
                                                    @Valid @RequestBody UpdateUserSettingsRequest request) {
        return ApiResponse.ok(userService.updateSettings(principal, userId, request),
                "사용자 설정이 변경되었습니다.");
    }

    @Operation(tags = "AAC User Setup", summary = "Figma 10 격자 설정")
    @PatchMapping("/{userId}/onboarding/grid")
    public ApiResponse<UserResponse> updateGrid(@AuthenticationPrincipal OidcUser principal,
                                                @PathVariable Long userId,
                                                @Valid @RequestBody UpdateGridRequest request) {
        return ApiResponse.ok(userService.updateGrid(principal, userId, request), "격자 설정이 변경되었습니다.");
    }

    @Operation(tags = "AAC User Setup", summary = "Figma 11 음성 설정")
    @PatchMapping("/{userId}/voice-settings")
    public ApiResponse<UserResponse> updateVoice(@AuthenticationPrincipal OidcUser principal,
                                                 @PathVariable Long userId,
                                                 @Valid @RequestBody UpdateVoiceSettingsRequest request) {
        return ApiResponse.ok(userService.updateVoice(principal, userId, request), "음성 설정이 변경되었습니다.");
    }

    @Operation(tags = "AAC User Setup", summary = "Figma 12 가입정보 확인")
    @GetMapping("/{userId}/onboarding-summary")
    public ApiResponse<OnboardingSummaryResponse> summary(@AuthenticationPrincipal OidcUser principal,
                                                           @PathVariable Long userId) {
        return ApiResponse.ok(userService.summary(principal, userId));
    }

    @Operation(tags = "AAC User Setup", summary = "AAC 사용자 설정 확정")
    @PostMapping("/{userId}/onboarding/confirm")
    public ApiResponse<OnboardingSummaryResponse> confirm(@AuthenticationPrincipal OidcUser principal,
                                                           @PathVariable Long userId) {
        return ApiResponse.ok(userService.confirm(principal, userId), "AAC 사용자 설정이 완료되었습니다.");
    }
    @GetMapping("/{userId}/favorites")
    public ApiResponse<List<SymbolResponse>> findFavorites(@AuthenticationPrincipal OidcUser principal,
                                                           @PathVariable Long userId) {
        return ApiResponse.ok(aacUsageService.findFavorites(principal, userId));
    }

    @PostMapping("/{userId}/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SymbolResponse> addFavorite(@AuthenticationPrincipal OidcUser principal,
                                                   @PathVariable Long userId,
                                                   @Valid @RequestBody AddFavoriteRequest request) {
        return ApiResponse.ok(aacUsageService.addFavorite(principal, userId, request.symbolId()),
                "즐겨찾기에 등록되었습니다.");
    }

    @DeleteMapping("/{userId}/favorites/{symbolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@AuthenticationPrincipal OidcUser principal,
                               @PathVariable Long userId, @PathVariable Long symbolId) {
        aacUsageService.removeFavorite(principal, userId, symbolId);
    }

    @PostMapping("/{userId}/usage-logs")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UsageLogResponse> createUsageLog(@AuthenticationPrincipal OidcUser principal,
                                                        @PathVariable Long userId,
                                                        @Valid @RequestBody CreateUsageLogRequest request) {
        return ApiResponse.ok(aacUsageService.createUsageLog(principal, userId, request),
                "사용 기록이 저장되었습니다.");
    }

    @GetMapping("/{userId}/recent-symbols")
    public ApiResponse<List<SymbolResponse>> findRecentSymbols(@AuthenticationPrincipal OidcUser principal,
                                                               @PathVariable Long userId,
                                                               @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(aacUsageService.findRecentSymbols(principal, userId, limit));
    }

    @GetMapping("/{userId}/guardians")
    public ApiResponse<List<GuardianResponse>> findGuardians(@AuthenticationPrincipal OidcUser principal,
                                                             @PathVariable Long userId) {
        return ApiResponse.ok(guardianService.findByUser(principal, userId));
    }

    @PostMapping("/{userId}/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GuardianResponse> linkGuardian(@AuthenticationPrincipal OidcUser principal,
                                                      @PathVariable Long userId,
                                                      @Valid @RequestBody LinkGuardianRequest request) {
        return ApiResponse.ok(guardianService.linkToUser(principal, userId, request),
                "사용자와 보호자가 연결되었습니다.");
    }
}
