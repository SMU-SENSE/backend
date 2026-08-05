package com.aac.ieojwo.auth.service;

import com.aac.ieojwo.account.domain.Account;
import com.aac.ieojwo.account.domain.OAuthProvider;
import com.aac.ieojwo.account.repository.OAuthAccountRepository;
import com.aac.ieojwo.common.exception.UnauthorizedException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentAccountService {

    private final OAuthAccountRepository oauthAccountRepository;

    public CurrentAccountService(OAuthAccountRepository oauthAccountRepository) {
        this.oauthAccountRepository = oauthAccountRepository;
    }

    public Account requireGoogleAccount(OidcUser principal) {
        if (principal == null || principal.getSubject() == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        return oauthAccountRepository
                .findByProviderAndProviderSubject(OAuthProvider.GOOGLE, principal.getSubject())
                .map(oauthAccount -> oauthAccount.getAccount())
                .orElseThrow(() -> new UnauthorizedException("로그인 계정을 찾을 수 없습니다."));
    }
}
