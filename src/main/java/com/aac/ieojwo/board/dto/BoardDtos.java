package com.aac.ieojwo.board.dto;

import com.aac.ieojwo.board.domain.AacCard;
import com.aac.ieojwo.board.domain.BoardCategory;
import com.aac.ieojwo.board.domain.ImageSourceType;
import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class BoardDtos {
    private BoardDtos() {
    }

    public record CategoryResponse(Long id, String name, String color, int displayOrder) {
        public static CategoryResponse from(BoardCategory category) {
            return new CategoryResponse(category.getId(), category.getName(), category.getColor(),
                    category.getDisplayOrder());
        }
    }

    @Schema(description = "사용자별 AAC 카드. text는 기존 클라이언트 호환용 displayText 별칭입니다.")
    public record CardResponse(
            Long id,
            Long categoryId,
            String text,
            String displayText,
            String imageUrl,
            ImageSourceType imageSourceType,
            String imageSourceName,
            String imageLicense,
            String imageAttributionUrl,
            String ttsText,
            boolean emergency,
            boolean favorite,
            boolean important,
            boolean systemCard,
            int displayOrder
    ) {
        public static CardResponse from(AacCard card) {
            return new CardResponse(
                    card.getId(), card.getCategory().getId(), card.getText(), card.getText(),
                    card.getImageUrl(), card.getImageSourceType(), card.getImageSourceName(),
                    card.getImageLicense(), card.getImageAttributionUrl(), card.getTtsText(),
                    card.isEmergency(), card.isFavorite(), card.isImportant(), card.isSystemCard(),
                    card.getDisplayOrder());
        }
    }

    public record BoardResponse(Long aacUserId, long version, GridSize gridSize, UserStatus status,
                                List<CategoryResponse> categories, List<CardResponse> cards) {
    }

    public record CreateCategoryRequest(@NotBlank @Size(max = 50) String name,
                                        @Size(max = 20) String color,
                                        @NotNull @Min(0) Integer displayOrder) {
    }

    public record UpdateCategoryRequest(@Size(max = 50) String name,
                                        @Size(max = 20) String color,
                                        @Min(0) Integer displayOrder) {
    }

    public record CreateCardRequest(
            @NotNull Long categoryId,
            @Size(max = 80) String text,
            @Size(max = 80) String displayText,
            @Size(max = 500) String imageUrl,
            ImageSourceType imageSourceType,
            @Size(max = 100) String imageSourceName,
            @Size(max = 100) String imageLicense,
            @Size(max = 500) String imageAttributionUrl,
            @Size(max = 200) String ttsText,
            boolean emergency,
            boolean important,
            @Min(0) int displayOrder
    ) {
        @AssertTrue(message = "displayText 또는 text 중 하나는 필수이며 둘 다 제공하면 값이 같아야 합니다.")
        public boolean isDisplayTextValid() {
            String legacy = normalize(text);
            String display = normalize(displayText);
            return (legacy != null || display != null) &&
                    (legacy == null || display == null || legacy.equals(display));
        }

        public String resolvedDisplayText() {
            String display = normalize(displayText);
            return display == null ? normalize(text) : display;
        }
    }

    public record UpdateCardRequest(
            Long categoryId,
            @Size(max = 80) String text,
            @Size(max = 80) String displayText,
            @Size(max = 500) String imageUrl,
            ImageSourceType imageSourceType,
            @Size(max = 100) String imageSourceName,
            @Size(max = 100) String imageLicense,
            @Size(max = 500) String imageAttributionUrl,
            @Size(max = 200) String ttsText,
            Boolean emergency,
            Boolean important,
            @Min(0) Integer displayOrder
    ) {
        @AssertTrue(message = "text와 displayText를 함께 제공하면 값이 같아야 합니다.")
        public boolean isDisplayTextValid() {
            String legacy = normalize(text);
            String display = normalize(displayText);
            return legacy == null || display == null || legacy.equals(display);
        }

        public String resolvedDisplayText() {
            String display = normalize(displayText);
            return display == null ? normalize(text) : display;
        }

        public boolean hasImageUpdate() {
            return imageUrl != null || imageSourceType != null || imageSourceName != null ||
                    imageLicense != null || imageAttributionUrl != null;
        }
    }

    public record FavoriteRequest(boolean favorite) {
    }

    public record BoardChangeEvent(long version, String changeType, CardResponse card,
                                   CategoryResponse category) {
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
