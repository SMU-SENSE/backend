package com.aac.ieojwo.auth.dto;

import com.aac.ieojwo.account.domain.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OnboardingRequest(
        @NotNull(message = "사용자 유형을 선택해주세요.")
        AccountType accountType,

        boolean termsOfServiceAgreed,
        boolean privacyPolicyAgreed,
        boolean marketingAgreed,

        @Pattern(
                regexp = "^$|^[0-9+() -]{8,30}$",
                message = "연락처 형식을 확인해주세요."
        )
        String phoneNumber
) {
}
