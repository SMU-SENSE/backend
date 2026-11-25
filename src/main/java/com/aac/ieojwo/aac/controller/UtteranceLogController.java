package com.aac.ieojwo.aac.controller;

import com.aac.ieojwo.aac.dto.CreateUtteranceLogRequest;
import com.aac.ieojwo.aac.dto.UtteranceLogResponse;
import com.aac.ieojwo.aac.service.AacUsageService;
import com.aac.ieojwo.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Utterance Logs", description = "문장(카드 묶음) 단위 사용 기록 — 개인 n-gram 복원용")
@RestController
@RequestMapping("/api/v1/me/aac-users/{userId}/utterance-logs")
public class UtteranceLogController {

    private final AacUsageService aacUsageService;

    public UtteranceLogController(AacUsageService aacUsageService) {
        this.aacUsageService = aacUsageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UtteranceLogResponse> create(@AuthenticationPrincipal OidcUser principal,
                                                    @PathVariable Long userId,
                                                    @Valid @RequestBody CreateUtteranceLogRequest request) {
        return ApiResponse.ok(aacUsageService.createUtteranceLog(principal, userId, request),
                "문장 사용 기록이 저장되었습니다.");
    }

    @GetMapping
    public ApiResponse<List<UtteranceLogResponse>> findAll(
            @AuthenticationPrincipal OidcUser principal,
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        return ApiResponse.ok(aacUsageService.findUtteranceLogs(principal, userId, since));
    }
}
