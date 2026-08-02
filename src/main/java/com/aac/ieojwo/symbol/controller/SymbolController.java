package com.aac.ieojwo.symbol.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.symbol.dto.*;
import com.aac.ieojwo.symbol.service.SymbolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SymbolController {

    private final SymbolService symbolService;

    public SymbolController(SymbolService symbolService) {
        this.symbolService = symbolService;
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.ok(symbolService.createCategory(request), "카테고리가 생성되었습니다.");
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> findCategories() {
        return ApiResponse.ok(symbolService.findCategories());
    }

    @PostMapping("/symbols")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SymbolResponse> createSymbol(@Valid @RequestBody CreateSymbolRequest request) {
        return ApiResponse.ok(symbolService.createSymbol(request), "상징이 생성되었습니다.");
    }

    @GetMapping("/symbols")
    public ApiResponse<List<SymbolResponse>> findSymbols(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean emergency
    ) {
        return ApiResponse.ok(symbolService.findSymbols(categoryId, emergency));
    }

    @GetMapping("/symbols/{symbolId}")
    public ApiResponse<SymbolResponse> findSymbol(@PathVariable Long symbolId) {
        return ApiResponse.ok(symbolService.findSymbol(symbolId));
    }
}
