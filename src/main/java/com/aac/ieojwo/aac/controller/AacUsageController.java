package com.aac.ieojwo.aac.controller;

import com.aac.ieojwo.aac.dto.AddFavoriteRequest;
import com.aac.ieojwo.aac.dto.CreateUsageLogRequest;
import com.aac.ieojwo.aac.dto.UsageLogResponse;
import com.aac.ieojwo.aac.service.AacUsageService;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.symbol.dto.SymbolResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}")
public class AacUsageController {

    private final AacUsageService aacUsageService;

    public AacUsageController(AacUsageService aacUsageService) {
        this.aacUsageService = aacUsageService;
    }

    @PostMapping("/favorites")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SymbolResponse> addFavorite(
            @PathVariable Long userId,
            @Valid @RequestBody AddFavoriteRequest request
    ) {
        return ApiResponse.ok(
                aacUsageService.addFavorite(userId, request.symbolId()),
                "즐겨찾기에 등록되었습니다."
        );
    }

    @GetMapping("/favorites")
    public ApiResponse<List<SymbolResponse>> findFavorites(@PathVariable Long userId) {
        return ApiResponse.ok(aacUsageService.findFavorites(userId));
    }

    @DeleteMapping("/favorites/{symbolId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(@PathVariable Long userId, @PathVariable Long symbolId) {
        aacUsageService.removeFavorite(userId, symbolId);
    }

    @PostMapping("/usage-logs")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UsageLogResponse> createUsageLog(
            @PathVariable Long userId,
            @Valid @RequestBody CreateUsageLogRequest request
    ) {
        return ApiResponse.ok(aacUsageService.createUsageLog(userId, request), "사용 기록이 저장되었습니다.");
    }

    @GetMapping("/recent-symbols")
    public ApiResponse<List<SymbolResponse>> findRecentSymbols(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ApiResponse.ok(aacUsageService.findRecentSymbols(userId, limit));
    }
}
