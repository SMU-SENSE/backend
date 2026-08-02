package com.aac.ieojwo.symbol.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "카테고리 코드는 필수입니다.")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "코드는 영문 대문자, 숫자, 밑줄만 사용할 수 있습니다.")
        @Size(max = 50, message = "코드는 50자 이하여야 합니다.")
        String code,

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
        String name,

        @Min(value = 0, message = "표시 순서는 0 이상이어야 합니다.")
        int displayOrder
) {
}
