package com.aac.ieojwo.symbol.service;

import com.aac.ieojwo.common.exception.ConflictException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.symbol.domain.SymbolCategory;
import com.aac.ieojwo.symbol.dto.*;
import com.aac.ieojwo.symbol.repository.SymbolCategoryRepository;
import com.aac.ieojwo.symbol.repository.SymbolRepository;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.service.AacUserAccessService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SymbolService {

    private static final String EMERGENCY_CATEGORY = "EMERGENCY";

    private final SymbolCategoryRepository categoryRepository;
    private final SymbolRepository symbolRepository;
    private final AacUserAccessService userAccessService;

    public SymbolService(SymbolCategoryRepository categoryRepository, SymbolRepository symbolRepository,
                         AacUserAccessService userAccessService) {
        this.categoryRepository = categoryRepository;
        this.symbolRepository = symbolRepository;
        this.userAccessService = userAccessService;
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
            symbols = symbolRepository.findAllByEmergencyTrueAndActiveTrueAndOwnerIsNullOrderByDisplayOrderAscIdAsc();
        } else if (categoryId != null) {
            getCategory(categoryId);
            symbols = symbolRepository.findAllByCategoryIdAndActiveTrueAndOwnerIsNullOrderByDisplayOrderAscIdAsc(categoryId);
        } else {
            symbols = symbolRepository.findAllByActiveTrueAndOwnerIsNullOrderByDisplayOrderAscIdAsc();
        }
        return symbols.stream().map(SymbolResponse::from).toList();
    }

    public SymbolResponse findSymbol(Long symbolId) {
        Symbol symbol = getSymbol(symbolId);
        if (symbol.isCustom()) {
            throw symbolNotFound(symbolId);
        }
        return SymbolResponse.from(symbol);
    }

    public Symbol getSymbol(Long symbolId) {
        return symbolRepository.findById(symbolId).orElseThrow(() -> symbolNotFound(symbolId));
    }

    /** 기본 카드이거나 이 사용자의 커스텀 카드일 때만 반환. 남의 커스텀 카드는 존재 자체를 숨기려고 404. */
    public Symbol getUsableSymbol(Long symbolId, AacUser user) {
        Symbol symbol = getSymbol(symbolId);
        if (!symbol.isUsableBy(user)) {
            throw symbolNotFound(symbolId);
        }
        return symbol;
    }

    /** 카드 ID(card_code) 목록을 요청 순서 그대로 상징으로 바꾼다. 중복 카드 허용. */
    public List<Symbol> getUsableCards(List<String> cardCodes, AacUser user) {
        Map<String, Symbol> found = symbolRepository.findAllByCardCodeIn(cardCodes).stream()
                .filter(symbol -> symbol.isUsableBy(user))
                .collect(Collectors.toMap(Symbol::getCardCode, Function.identity()));
        return cardCodes.stream()
                .map(code -> {
                    Symbol symbol = found.get(code);
                    if (symbol == null) {
                        throw new ResourceNotFoundException("카드를 찾을 수 없습니다. cardCode=" + code);
                    }
                    return symbol;
                })
                .toList();
    }

    @Transactional
    public SymbolResponse createCustomSymbol(OidcUser principal, Long userId, CreateCustomSymbolRequest request) {
        AacUser user = userAccessService.requireAccessibleUser(principal, userId);
        SymbolCategory category = getCategory(request.categoryId());
        String name = request.name().trim();
        String ttsText = normalize(request.ttsText());
        Symbol symbol = Symbol.createCustom(user, category, name, normalize(request.imageUrl()),
                ttsText == null ? name : ttsText, EMERGENCY_CATEGORY.equals(category.getCode()));
        return SymbolResponse.from(symbolRepository.save(symbol));
    }

    public List<SymbolResponse> findCustomSymbols(OidcUser principal, Long userId) {
        userAccessService.requireAccessibleUser(principal, userId);
        return symbolRepository.findAllByOwnerIdAndActiveTrueOrderByIdAsc(userId)
                .stream()
                .map(SymbolResponse::from)
                .toList();
    }

    @Transactional
    public void deleteCustomSymbol(OidcUser principal, Long userId, Long symbolId) {
        AacUser user = userAccessService.requireAccessibleUser(principal, userId);
        Symbol symbol = getSymbol(symbolId);
        if (!symbol.isCustom() || !symbol.isUsableBy(user) || !symbol.isActive()) {
            throw symbolNotFound(symbolId);
        }
        symbol.deactivate();
    }

    private ResourceNotFoundException symbolNotFound(Long symbolId) {
        return new ResourceNotFoundException("상징을 찾을 수 없습니다. symbolId=" + symbolId);
    }

    private SymbolCategory getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다. categoryId=" + categoryId));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
