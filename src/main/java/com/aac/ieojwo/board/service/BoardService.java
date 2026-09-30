package com.aac.ieojwo.board.service;

import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.board.domain.AacCard;
import com.aac.ieojwo.board.domain.BoardCategory;
import com.aac.ieojwo.board.domain.CardUsageLog;
import com.aac.ieojwo.board.domain.ImageSourceType;
import com.aac.ieojwo.board.dto.BoardDtos.BoardChangeEvent;
import com.aac.ieojwo.board.dto.BoardDtos.BoardResponse;
import com.aac.ieojwo.board.dto.BoardDtos.CardResponse;
import com.aac.ieojwo.board.dto.BoardDtos.CategoryResponse;
import com.aac.ieojwo.board.dto.BoardDtos.CreateCardRequest;
import com.aac.ieojwo.board.dto.BoardDtos.CreateCategoryRequest;
import com.aac.ieojwo.board.dto.BoardDtos.FavoriteRequest;
import com.aac.ieojwo.board.dto.BoardDtos.UpdateCardRequest;
import com.aac.ieojwo.board.dto.BoardDtos.UpdateCategoryRequest;
import com.aac.ieojwo.board.repository.AacCardRepository;
import com.aac.ieojwo.board.repository.BoardCategoryRepository;
import com.aac.ieojwo.board.repository.CardUsageLogRepository;
import com.aac.ieojwo.common.exception.BadRequestException;
import com.aac.ieojwo.common.exception.ConflictException;
import com.aac.ieojwo.common.exception.ForbiddenException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.symbol.domain.SymbolCategory;
import com.aac.ieojwo.symbol.repository.SymbolCategoryRepository;
import com.aac.ieojwo.symbol.repository.SymbolRepository;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.user.service.AacUserAccessService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BoardService {
    private final BoardCategoryRepository categories;
    private final AacCardRepository cards;
    private final SymbolCategoryRepository catalogCategories;
    private final SymbolRepository catalogSymbols;
    private final AacUserAccessService access;
    private final LiveEventService live;
    private final CardUsageLogRepository usageLogs;

    public BoardService(BoardCategoryRepository categories, AacCardRepository cards,
                        SymbolCategoryRepository catalogCategories, SymbolRepository catalogSymbols,
                        AacUserAccessService access, LiveEventService live,
                        CardUsageLogRepository usageLogs) {
        this.categories = categories;
        this.cards = cards;
        this.catalogCategories = catalogCategories;
        this.catalogSymbols = catalogSymbols;
        this.access = access;
        this.live = live;
        this.usageLogs = usageLogs;
    }

    @Transactional
    public BoardResponse get(OidcUser principal, Long userId) {
        return get(access.requireAccessibleUser(principal, userId));
    }

    @Transactional
    public BoardResponse getForDevice(AacUser user) {
        return get(user);
    }

    private BoardResponse get(AacUser user) {
        initialize(user);
        return response(user);
    }

    private void initialize(AacUser user) {
        if (categories.existsByUserIdAndActiveTrue(user.getId())) return;
        Map<Long, BoardCategory> categoryMap = new HashMap<>();
        for (SymbolCategory source : catalogCategories.findAllByActiveTrueOrderByDisplayOrderAscIdAsc()) {
            BoardCategory category = categories.save(BoardCategory.create(
                    user, source.getName(), null, source.getDisplayOrder()));
            categoryMap.put(source.getId(), category);
        }
        catalogSymbols.findAllByActiveTrueOrderByDisplayOrderAscIdAsc().forEach(source -> {
            BoardCategory category = categoryMap.get(source.getCategory().getId());
            if (category != null) {
                cards.save(AacCard.createSystem(user, category, source.getName(), source.getImageUrl(),
                        source.getTtsText(), source.isEmergency(), source.getDisplayOrder()));
            }
        });
        user.incrementBoardVersion();
        live.publish(user.getId(), "BOARD_UPDATED",
                new BoardChangeEvent(user.getBoardVersion(), "BOARD_INITIALIZED", null, null));
    }

    private BoardResponse response(AacUser user) {
        return new BoardResponse(
                user.getId(), user.getBoardVersion(), user.getGridSize(), user.getStatus(),
                categories.findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(user.getId())
                        .stream().map(CategoryResponse::from).toList(),
                cards.findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(user.getId())
                        .stream().map(CardResponse::from).toList());
    }

    @Transactional
    public CategoryResponse createCategory(OidcUser principal, Long userId, CreateCategoryRequest request) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        BoardCategory category = categories.save(BoardCategory.create(
                user, request.name(), request.color(), request.displayOrder()));
        CategoryResponse response = CategoryResponse.from(category);
        changed(user, "CATEGORY_CREATED", null, response);
        return response;
    }

    @Transactional
    public CategoryResponse updateCategory(OidcUser principal, Long userId, Long categoryId,
                                           UpdateCategoryRequest request) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        BoardCategory category = category(user, categoryId);
        category.update(request.name(), request.color(), request.displayOrder());
        CategoryResponse response = CategoryResponse.from(category);
        changed(user, "CATEGORY_UPDATED", null, response);
        return response;
    }

    @Transactional
    public void deleteCategory(OidcUser principal, Long userId, Long categoryId) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        BoardCategory category = category(user, categoryId);
        if (!cards.findAllByCategoryIdAndActiveTrue(categoryId).isEmpty()) {
            throw new ConflictException("카테고리의 카드를 먼저 삭제하거나 이동해주세요.");
        }
        CategoryResponse response = CategoryResponse.from(category);
        category.deactivate();
        changed(user, "CATEGORY_DELETED", null, response);
    }

    @Transactional
    public CardResponse createCard(OidcUser principal, Long userId, CreateCardRequest request) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        ImageSourceType sourceType = resolveSourceType(userId, request.imageUrl(), request.imageSourceType(), null);
        validateImage(userId, request.imageUrl(), sourceType, request.imageSourceName(),
                request.imageLicense(), request.imageAttributionUrl());
        AacCard card = cards.save(AacCard.create(
                user, category(user, request.categoryId()), request.resolvedDisplayText(),
                request.imageUrl(), request.ttsText(), request.emergency(), request.displayOrder(),
                sourceType, request.imageSourceName(), request.imageLicense(),
                request.imageAttributionUrl(), request.important(), false));
        CardResponse response = CardResponse.from(card);
        changed(user, "CARD_CREATED", response, null);
        return response;
    }

    @Transactional
    public CardResponse updateCard(OidcUser principal, Long userId, Long cardId, UpdateCardRequest request) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        AacCard card = card(user, cardId);
        card.update(request.categoryId() == null ? null : category(user, request.categoryId()),
                request.resolvedDisplayText(), request.ttsText(), request.emergency(),
                request.displayOrder(), request.important());
        if (request.hasImageUpdate()) {
            ImageSourceType sourceType = resolveSourceType(
                    userId, request.imageUrl(), request.imageSourceType(), card.getImageSourceType());
            validateImage(userId, request.imageUrl(), sourceType, request.imageSourceName(),
                    request.imageLicense(), request.imageAttributionUrl());
            card.updateImage(request.imageUrl(), sourceType, request.imageSourceName(),
                    request.imageLicense(), request.imageAttributionUrl());
        }
        CardResponse response = CardResponse.from(card);
        changed(user, "CARD_UPDATED", response, null);
        return response;
    }

    @Transactional
    public CardResponse favorite(OidcUser principal, Long userId, Long cardId, FavoriteRequest request) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        AacCard card = card(user, cardId);
        card.setFavorite(request.favorite());
        CardResponse response = CardResponse.from(card);
        changed(user, "CARD_FAVORITE_UPDATED", response, null);
        return response;
    }

    @Transactional
    public void deleteCard(OidcUser principal, Long userId, Long cardId) {
        AacUser user = access.requireAccessibleUser(principal, userId);
        AacCard card = card(user, cardId);
        CardResponse response = CardResponse.from(card);
        card.deactivate();
        changed(user, "CARD_DELETED", response, null);
    }

    @Transactional
    public Long recordUsage(AacDevice device, Long cardId, UsageAction action, String spokenText,
                            Instant occurredAt) {
        AacUser user = device.getUser();
        AacCard card = cardId == null ? null : card(user, cardId);
        if (action != UsageAction.SPEAK && card == null) {
            throw new BadRequestException("카드 선택/취소 기록에는 cardId가 필요합니다.");
        }
        String actualSpokenText = spokenText;
        if (action == UsageAction.SPEAK && (actualSpokenText == null || actualSpokenText.isBlank())) {
            actualSpokenText = card == null ? null : card.getTtsText();
        }
        if (action == UsageAction.SPEAK && (actualSpokenText == null || actualSpokenText.isBlank())) {
            throw new BadRequestException("TTS 실행 기록에는 cardId 또는 spokenText가 필요합니다.");
        }
        CardUsageLog log = usageLogs.save(CardUsageLog.create(
                device, card, action, actualSpokenText, occurredAt == null ? Instant.now() : occurredAt));
        live.publish(user.getId(), "CARD_USED", Map.of(
                "cardId", cardId == null ? "" : cardId, "action", action.name(),
                "occurredAt", log.getOccurredAt().toString()));
        return log.getId();
    }

    private BoardCategory category(AacUser user, Long categoryId) {
        BoardCategory category = categories.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다."));
        if (!category.getUser().getId().equals(user.getId()) || !category.isActive()) {
            throw new ForbiddenException("다른 사용자의 카테고리입니다.");
        }
        return category;
    }

    private AacCard card(AacUser user, Long cardId) {
        AacCard card = cards.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("카드를 찾을 수 없습니다."));
        if (!card.getUser().getId().equals(user.getId()) || !card.isActive()) {
            throw new ForbiddenException("다른 사용자의 카드입니다.");
        }
        return card;
    }

    private void changed(AacUser user, String type, CardResponse card, CategoryResponse category) {
        user.incrementBoardVersion();
        live.publish(user.getId(), "BOARD_UPDATED",
                new BoardChangeEvent(user.getBoardVersion(), type, card, category));
    }

    private ImageSourceType resolveSourceType(Long userId, String imageUrl, ImageSourceType requested,
                                              ImageSourceType current) {
        if (requested != null) return requested;
        if (imageUrl != null && imageUrl.startsWith(userMediaPrefix(userId))) return ImageSourceType.USER_UPLOAD;
        if (imageUrl != null && !imageUrl.isBlank()) return ImageSourceType.EXTERNAL_ALLOWED;
        return current == null ? ImageSourceType.SYSTEM_DEFAULT : current;
    }

    private void validateImage(Long userId, String imageUrl, ImageSourceType sourceType,
                               String sourceName, String license, String attributionUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            if (sourceType != ImageSourceType.SYSTEM_DEFAULT) {
                throw new BadRequestException("사용자 또는 외부 이미지는 imageUrl이 필요합니다.");
            }
            return;
        }
        if (sourceType == ImageSourceType.USER_UPLOAD && !imageUrl.startsWith(userMediaPrefix(userId))) {
            throw new BadRequestException("사용자 업로드 이미지는 해당 AAC 사용자의 인증 미디어 URL이어야 합니다.");
        }
        if (sourceType == ImageSourceType.EXTERNAL_ALLOWED) {
            try {
                if (!"https".equalsIgnoreCase(URI.create(imageUrl).getScheme())) throw new IllegalArgumentException();
            } catch (IllegalArgumentException exception) {
                throw new BadRequestException("외부 이미지는 유효한 HTTPS URL이어야 합니다.");
            }
            if (isBlank(sourceName) || isBlank(license) || isBlank(attributionUrl)) {
                throw new BadRequestException("외부 이미지는 출처명, 라이선스, 출처 URL이 필요합니다.");
            }
            try {
                if (!"https".equalsIgnoreCase(URI.create(attributionUrl).getScheme())) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException exception) {
                throw new BadRequestException("외부 이미지 출처 URL은 유효한 HTTPS URL이어야 합니다.");
            }
        }
    }

    private String userMediaPrefix(Long userId) {
        return "/api/v1/me/aac-users/" + userId + "/media/images/";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
