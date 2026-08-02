package com.aac.ieojwo.auth.dto;

import com.aac.ieojwo.account.domain.Account;
import com.aac.ieojwo.account.domain.AccountStatus;
import com.aac.ieojwo.account.domain.AccountType;

public record AccountResponse(
        Long accountId,
        String email,
        String name,
        String profileImageUrl,
        AccountType accountType,
        boolean onboardingCompleted,
        AccountStatus status
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getName(),
                account.getProfileImageUrl(),
                account.getAccountType(),
                account.isOnboardingCompleted(),
                account.getStatus()
        );
    }
}
