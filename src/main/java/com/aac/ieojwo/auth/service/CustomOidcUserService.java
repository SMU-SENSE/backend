package com.aac.ieojwo.auth.service;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService {

    private final OidcUserService delegate = new OidcUserService();
    private final AuthService authService;

    public CustomOidcUserService(AuthService authService) {
        this.authService = authService;
    }

    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser user = delegate.loadUser(userRequest);

        Boolean emailVerified = user.getClaimAsBoolean("email_verified");
        if (Boolean.FALSE.equals(emailVerified)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email"),
                    "인증되지 않은 구글 이메일입니다."
            );
        }

        authService.upsertGoogleAccount(
                user.getSubject(),
                user.getClaimAsString("email"),
                user.getClaimAsString("name"),
                user.getClaimAsString("picture")
        );

        return user;
    }
}
