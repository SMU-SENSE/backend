package com.aac.ieojwo.auth.security;

import com.aac.ieojwo.auth.handler.OAuth2FailureHandler;
import com.aac.ieojwo.auth.handler.OAuth2SuccessHandler;
import com.aac.ieojwo.auth.service.CustomOidcUserService;
import com.aac.ieojwo.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomOidcUserService customOidcUserService,
            OAuth2SuccessHandler successHandler,
            OAuth2FailureHandler failureHandler,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            ObjectMapper objectMapper
    ) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/health",
                                "/h2-console/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService::loadUser)
                        )
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            objectMapper.writeValue(
                                    response.getWriter(),
                                    ApiResponse.ok(null, "로그아웃되었습니다.")
                            );
                        })
                );

        return http.build();
    }
}
