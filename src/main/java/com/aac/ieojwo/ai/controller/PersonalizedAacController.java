package com.aac.ieojwo.ai.controller;

import com.aac.ieojwo.ai.dto.PersonalizedAacContext;
import com.aac.ieojwo.ai.dto.SentenceRecommendationRequest;
import com.aac.ieojwo.ai.dto.SentenceRecommendationResponse;
import com.aac.ieojwo.ai.service.AacSentenceRecommendationService;
import com.aac.ieojwo.ai.service.PersonalizedAacContextBuilder;
import com.aac.ieojwo.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/me/aac-users/{userId}/ai")
@Tag(name = "Personalized AAC")
public class PersonalizedAacController {
    private final PersonalizedAacContextBuilder contextBuilder;
    private final AacSentenceRecommendationService recommendationService;

    public PersonalizedAacController(PersonalizedAacContextBuilder contextBuilder,
                                     AacSentenceRecommendationService recommendationService) {
        this.contextBuilder = contextBuilder;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/context")
    @Operation(summary = "개인화 AAC 컨텍스트 조회")
    public ApiResponse<PersonalizedAacContext> context(
            @AuthenticationPrincipal OidcUser principal, @PathVariable Long userId,
            @RequestParam(required = false) @Size(max = 500) String currentSituation) {
        return ApiResponse.ok(contextBuilder.build(principal, userId, currentSituation));
    }

    @PostMapping("/recommendations")
    @Operation(summary = "단일 AAC 문장 추천",
            description = "설정된 provider가 없으면 503을 반환합니다. 응답 data에는 sentence 하나만 포함합니다.")
    public ApiResponse<SentenceRecommendationResponse> recommend(
            @AuthenticationPrincipal OidcUser principal, @PathVariable Long userId,
            @Valid @RequestBody SentenceRecommendationRequest request) {
        return ApiResponse.ok(recommendationService.recommend(principal, userId, request.currentSituation()));
    }
}
