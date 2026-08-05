package com.aac.ieojwo.auth.handler;

import com.aac.ieojwo.auth.dto.AccountResponse;
import com.aac.ieojwo.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final String frontendBaseUrl;
    private final AuthService authService;

    public OAuth2SuccessHandler(@Value("${app.frontend-base-url:http://localhost:3000}") String frontendBaseUrl,
                                AuthService authService) {
        this.frontendBaseUrl = stripTrailingSlash(frontendBaseUrl);
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        AccountResponse account = authService.getCurrentAccount((OidcUser) authentication.getPrincipal());
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/oauth/callback")
                .queryParam("login", "success")
                .queryParam("onboardingCompleted", account.onboardingCompleted())
                .build()
                .encode()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
