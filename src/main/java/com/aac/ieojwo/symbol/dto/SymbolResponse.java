package com.aac.ieojwo.symbol.dto;

import com.aac.ieojwo.symbol.domain.Symbol;

public record SymbolResponse(
        Long id,
        Long categoryId,
        String categoryCode,
        String categoryName,
        String name,
        String imageUrl,
        String ttsText,
        boolean emergency,
        int displayOrder,
        boolean active
) {
    public static SymbolResponse from(Symbol symbol) {
        return new SymbolResponse(
                symbol.getId(),
                symbol.getCategory().getId(),
                symbol.getCategory().getCode(),
                symbol.getCategory().getName(),
                symbol.getName(),
                symbol.getImageUrl(),
                symbol.getTtsText(),
                symbol.isEmergency(),
                symbol.getDisplayOrder(),
                symbol.isActive()
        );
    }
}
