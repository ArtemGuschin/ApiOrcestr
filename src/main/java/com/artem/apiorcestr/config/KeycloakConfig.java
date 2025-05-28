package com.artem.apiorcestr.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Data
@Configuration
public class KeycloakConfig {

    @Value("${keycloak.admin.url}")
    private String adminUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Bean
    public WebClient keycloakWebClient() {
        return WebClient.builder()
                .baseUrl(adminUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
