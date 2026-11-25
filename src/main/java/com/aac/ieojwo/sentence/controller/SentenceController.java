package com.aac.ieojwo.sentence.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.sentence.dto.GenerateSentenceRequest;
import com.aac.ieojwo.sentence.dto.SentenceResponse;
import com.aac.ieojwo.sentence.service.SentenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sentences", description = "카드 순서 → 자연어 문장 변환 (LLM 중계)")
@RestController
@RequestMapping("/api/v1/me/aac-users/{userId}/sentences")
public class SentenceController {

    private final SentenceService sentenceService;

    public SentenceController(SentenceService sentenceService) {
        this.sentenceService = sentenceService;
    }

    @PostMapping
    public ApiResponse<SentenceResponse> generate(@AuthenticationPrincipal OidcUser principal,
                                                  @PathVariable Long userId,
                                                  @Valid @RequestBody GenerateSentenceRequest request) {
        return ApiResponse.ok(sentenceService.generate(principal, userId, request));
    }
}
