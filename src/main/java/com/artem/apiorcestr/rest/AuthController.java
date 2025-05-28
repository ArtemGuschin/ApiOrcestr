package com.artem.apiorcestr.rest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final WebClient webClient;

    public AuthController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    @PostMapping("/token")
    public Mono<ResponseEntity<TokenResponse>> getToken(@RequestBody TokenRequest request) {
        return webClient.post()
                .uri("/realms/myrealm/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(request.toFormData()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).build()));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshToken(@RequestBody RefreshRequest request) {
        return webClient.post()
                .uri("/realms/myrealm/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(request.toFormData()))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(401).build()));
    }

    public record TokenRequest(String grantType, String username, String password) {
        public MultiValueMap<String, String> toFormData() {
            return new LinkedMultiValueMap<>() {{
                add("grant_type", grantType);
                add("client_id", "webapp");
                add("client_secret", "supersecret");
                add("username", username);
                add("password", password);
            }};
        }
    }

    public record RefreshRequest(String grantType, String refreshToken) {
        public MultiValueMap<String, String> toFormData() {
            return new LinkedMultiValueMap<>() {{
                add("grant_type", grantType);
                add("client_id", "webapp");
                add("client_secret", "supersecret");
                add("refresh_token", refreshToken);
            }};
        }
    }

    public record TokenResponse(
            String access_token,
            String refresh_token,
            long expires_in,
            String token_type,
            String scope
    ) {}
}
