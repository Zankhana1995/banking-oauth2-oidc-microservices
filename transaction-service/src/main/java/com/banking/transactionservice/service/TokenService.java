package com.banking.transactionservice.service;

import com.banking.transactionservice.config.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final WebClient webClient;
    private final KeycloakProperties properties;

    // Token fetched dynamically from Keycloak
    public String getToken() {
        @SuppressWarnings("unchecked")
        Map<String, Object> response = webClient.post()
                .uri(properties.getTokenUrl())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", properties.getClientId())
                        .with("client_secret", properties.getClientSecret()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Failed to retrieve access token");
        }

        return response.get("access_token").toString();
    }
}