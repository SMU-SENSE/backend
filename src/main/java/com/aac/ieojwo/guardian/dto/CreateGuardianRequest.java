package com.aac.ieojwo.guardian.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGuardianRequest(
        @NotBlank(message = "보호자 이름은 필수입니다.")
        @Size(max = 50, message = "보호자 이름은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
        String phone
) {
}
