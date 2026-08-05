package com.aac.ieojwo.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI malmoaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Malmoa Backend API")
                        .version("v1")
                        .description("Session-cookie API. Mutating requests require the X-XSRF-TOKEN header."))
                .tags(List.of(
                        new Tag().name("Auth"),
                        new Tag().name("AAC Users"),
                        new Tag().name("Guardians"),
                        new Tag().name("Symbols"),
                        new Tag().name("Favorites"),
                        new Tag().name("Usage Logs"),
                        new Tag().name("Health")
                ))
                .components(new Components().addSecuritySchemes("sessionCookie",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Created by Google OIDC login")));
    }
}
