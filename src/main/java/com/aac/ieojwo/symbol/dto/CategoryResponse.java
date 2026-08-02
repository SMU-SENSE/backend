package com.aac.ieojwo.symbol.dto;

import com.aac.ieojwo.symbol.domain.SymbolCategory;

public record CategoryResponse(
        Long id,
        String code,
        String name,
        int displayOrder,
        boolean active
) {
    public static CategoryResponse from(SymbolCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDisplayOrder(),
                category.isActive()
        );
    }
}
