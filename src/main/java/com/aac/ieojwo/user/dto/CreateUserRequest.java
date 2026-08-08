package com.aac.ieojwo.user.dto;

import com.aac.ieojwo.guardian.domain.RelationshipType;
import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateUserRequest(
        @Schema(example = "민수") @NotBlank @Size(max = 50) String name,
        @Schema(example = "2012-01-15") @NotNull @PastOrPresent LocalDate birthDate,
        @Schema(example = "PARENT") @NotNull RelationshipType relationshipType,
        @Size(max = 100) String relationshipDetail,
        @NotBlank @Size(max = 30) String emergencyContact,
        @Size(max = 1000) String notes,
        @Size(max = 500) String profileImageUrl,
        @Schema(description = "레거시 호환. 생략 시 SIMPLE") UserMode mode,
        @Schema(description = "레거시 호환. 생략 시 GRID_3X3") GridSize gridSize
) {
    @AssertTrue(message = "OTHER 관계에는 relationshipDetail이 필수입니다.")
    public boolean isRelationshipDetailValid() {
        return relationshipType != RelationshipType.OTHER || (relationshipDetail != null && !relationshipDetail.isBlank());
    }
}
