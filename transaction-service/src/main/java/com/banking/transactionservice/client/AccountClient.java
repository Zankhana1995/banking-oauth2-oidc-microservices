package com.banking.transactionservice.client;

import com.banking.transactionservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AccountClient {

    private final WebClient webClient;
    private final TokenService tokenService;

    public void validateAccount(Long accountId) {

        String token = tokenService.getToken();

        webClient.get()
                .uri("http://localhost:8081/accounts/" + accountId)
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}