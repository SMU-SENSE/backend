package com.aac.ieojwo.guardian.service;

import com.aac.ieojwo.account.domain.Account;
import com.aac.ieojwo.auth.service.CurrentAccountService;
import com.aac.ieojwo.common.exception.ForbiddenException;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.repository.GuardianRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GuardianAccessService {
    private final CurrentAccountService currentAccountService;
    private final GuardianRepository guardianRepository;

    public GuardianAccessService(CurrentAccountService currentAccountService, GuardianRepository guardianRepository) {
        this.currentAccountService = currentAccountService;
        this.guardianRepository = guardianRepository;
    }

    public Guardian requireCurrentGuardian(OidcUser principal) {
        Account account = currentAccountService.requireGoogleAccount(principal);
        return guardianRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new ForbiddenException("보호자 온보딩이 필요합니다."));
    }
}
