package com.banking.transactionservice.client;

import com.banking.transactionservice.dto.AccountResponse;
import com.banking.transactionservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AccountClient {

    private final WebClient webClient;
    private final TokenService tokenService;

    public AccountResponse getAccount(Long id) {
        String token = tokenService.getToken();

        return webClient.get()
                .uri("http://localhost:8081/accounts/" + id)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(AccountResponse.class)
                .block();
    }

    public void credit(Long id, BigDecimal amount) {

        String token = tokenService.getToken();

        webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8081)
                        .path("/accounts/{id}/credit")
                        .queryParam("amount", amount)
                        .build(id))
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void debit(Long id, BigDecimal amount) {

        String token = tokenService.getToken();

        webClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("localhost")
                        .port(8081)
                        .path("/accounts/{id}/debit")
                        .queryParam("amount", amount)
                        .build(id))
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}