package com.aac.ieojwo.symbol.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.symbol.dto.CreateCustomSymbolRequest;
import com.aac.ieojwo.symbol.dto.SymbolResponse;
import com.aac.ieojwo.symbol.service.SymbolService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Custom Symbols", description = "AAC 사용자별 커스텀 카드")
@RestController
@RequestMapping("/api/v1/me/aac-users/{userId}/custom-symbols")
public class CustomSymbolController {

    private final SymbolService symbolService;

    public CustomSymbolController(SymbolService symbolService) {
        this.symbolService = symbolService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SymbolResponse> create(@AuthenticationPrincipal OidcUser principal,
                                              @PathVariable Long userId,
                                              @Valid @RequestBody CreateCustomSymbolRequest request) {
        return ApiResponse.ok(symbolService.createCustomSymbol(principal, userId, request),
                "커스텀 카드가 생성되었습니다.");
    }

    @GetMapping
    public ApiResponse<List<SymbolResponse>> findAll(@AuthenticationPrincipal OidcUser principal,
                                                     @PathVariable Long userId) {
        return ApiResponse.ok(symbolService.findCustomSymbols(principal, userId));
    }

    @DeleteMapping("/{symbolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal OidcUser principal,
                       @PathVariable Long userId, @PathVariable Long symbolId) {
        symbolService.deleteCustomSymbol(principal, userId, symbolId);
    }
}
