package com.banking.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
class ApiGatewayApplicationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorHealthIsPublic() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unknownEndpointRequiresAuthentication() {
        webTestClient.get()
                .uri("/unknown")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void accountsEndpointRejectsJwtWithoutRequiredAuthority() {
        webTestClient
                .mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_transaction.write")))
                .get()
                .uri("/accounts/demo")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void authenticatedRequestToUnknownEndpointReturnsNotFound() {
        webTestClient
                .mutateWith(mockJwt())
                .get()
                .uri("/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

}
