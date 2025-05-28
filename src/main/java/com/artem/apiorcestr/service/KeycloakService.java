package com.artem.apiorcestr.service;

import com.artem.apiorcestr.config.KeycloakConfig;
import com.artem.apiorcestr.dto.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final WebClient keycloakWebClient;
    private final KeycloakConfig config;

    public Mono<Void> createUser(UserCreateRequest request) {
        return obtainAdminToken()
                .flatMap(token -> sendCreateUserRequest(request, token))
                .onErrorMap(e -> new KeycloakIntegrationException("User creation failed: " + e.getMessage()))
                .doOnSuccess(v -> log.info("User {} created successfully", request.username()));
    }

    private Mono<String> obtainAdminToken() {
        return keycloakWebClient.post()
                .uri("/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", config.getClientId())
                        .with("client_secret", config.getClientSecret()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Keycloak auth error: " + error)))
                )
                .bodyToMono(TokenResponse.class)
                .map(token -> "Bearer " + token.access_token());
    }

    private Mono<Void> sendCreateUserRequest(UserCreateRequest request, String token) {
        Map<String, Object> credentials = Map.of(
                "type", "password",
                "value", request.password(),
                "temporary", false
        );

        Map<String, Object> user = Map.of(
                "username", request.username(),
                "email", request.email(),
                "firstName", request.firstName(),
                "lastName", request.lastName(),
                "enabled", request.enabled(),
                "credentials", List.of(credentials)
        );

        return keycloakWebClient.post()
                .uri("/admin/realms/{realm}/users", config.getRealm())
                .header(HttpHeaders.AUTHORIZATION, token)
                .bodyValue(user)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("Keycloak API error: " + error)))
                )
                .bodyToMono(Void.class);
    }

    private record TokenResponse(String access_token) {}

    public static class KeycloakIntegrationException extends RuntimeException {
        public KeycloakIntegrationException(String message) {
            super(message);
        }
    }
}