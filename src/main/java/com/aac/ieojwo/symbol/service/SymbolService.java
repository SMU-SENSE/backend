package com.aac.ieojwo.symbol.service;

import com.aac.ieojwo.common.exception.ConflictException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.symbol.domain.SymbolCategory;
import com.aac.ieojwo.symbol.dto.*;
import com.aac.ieojwo.symbol.repository.SymbolCategoryRepository;
import com.aac.ieojwo.symbol.repository.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SymbolService {

    private final SymbolCategoryRepository categoryRepository;
    private final SymbolRepository symbolRepository;

    public SymbolService(SymbolCategoryRepository categoryRepository, SymbolRepository symbolRepository) {
        this.categoryRepository = categoryRepository;
        this.symbolRepository = symbolRepository;
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String code = request.code().trim().toUpperCase();
        if (categoryRepository.existsByCode(code)) {
            throw new ConflictException("이미 등록된 카테고리 코드입니다. code=" + code);
        }
        SymbolCategory category = SymbolCategory.create(code, request.name().trim(), request.displayOrder());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public List<CategoryResponse> findCategories() {
        return categoryRepository.findAllByActiveTrueOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public SymbolResponse createSymbol(CreateSymbolRequest request) {
        SymbolCategory category = getCategory(request.categoryId());
        Symbol symbol = Symbol.create(
                category,
                request.name().trim(),
                normalize(request.imageUrl()),
                request.ttsText().trim(),
                request.emergency(),
                request.displayOrder()
        );
        return SymbolResponse.from(symbolRepository.save(symbol));
    }

    public List<SymbolResponse> findSymbols(Long categoryId, Boolean emergency) {
        List<Symbol> symbols;
        if (Boolean.TRUE.equals(emergency)) {
            symbols = symbolRepository.findAllByEmergencyTrueAndActiveTrueOrderByDisplayOrderAscIdAsc();
        } else if (categoryId != null) {
            getCategory(categoryId);
            symbols = symbolRepository.findAllByCategoryIdAndActiveTrueOrderByDisplayOrderAscIdAsc(categoryId);
        } else {
            symbols = symbolRepository.findAllByActiveTrueOrderByDisplayOrderAscIdAsc();
        }
        return symbols.stream().map(SymbolResponse::from).toList();
    }

    public SymbolResponse findSymbol(Long symbolId) {
        return SymbolResponse.from(getSymbol(symbolId));
    }

    public Symbol getSymbol(Long symbolId) {
        return symbolRepository.findById(symbolId)
                .orElseThrow(() -> new ResourceNotFoundException("상징을 찾을 수 없습니다. symbolId=" + symbolId));
    }

    private SymbolCategory getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다. categoryId=" + categoryId));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
