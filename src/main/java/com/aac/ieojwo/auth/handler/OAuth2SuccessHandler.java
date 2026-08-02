package com.aac.ieojwo.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final String frontendBaseUrl;

    public OAuth2SuccessHandler(
            @Value("${app.frontend-base-url:http://localhost:3000}") String frontendBaseUrl
    ) {
        this.frontendBaseUrl = stripTrailingSlash(frontendBaseUrl);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        response.sendRedirect(frontendBaseUrl + "/oauth/callback?login=success");
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
