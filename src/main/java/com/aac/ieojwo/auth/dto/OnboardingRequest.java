package com.aac.ieojwo.auth.dto;

import com.aac.ieojwo.account.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OnboardingRequest(
        @Schema(description = "계정 유형", example = "GUARDIAN")
        @NotNull(message = "사용자 유형을 선택해주세요.")
        AccountType accountType,

        @Schema(description = "서비스 이용약관 동의 여부")
        boolean termsOfServiceAgreed,
        @Schema(description = "개인정보 처리방침 동의 여부")
        boolean privacyPolicyAgreed,
        @Schema(description = "선택 마케팅 동의 여부")
        boolean marketingAgreed,

        @Pattern(
                regexp = "^$|^[0-9+() -]{8,30}$",
                message = "연락처 형식을 확인해주세요."
        )
        String phoneNumber
) {
}
