package com.banking.transactionservice.integration.controller;

import com.banking.transactionservice.TransactionServiceApplication;
import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.dto.AccountResponse;
import com.banking.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TransactionServiceApplication.class)
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private AccountClient accountClient;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void transferRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferRequest()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(accountClient);
    }

    @Test
    void transferRequiresUserOrAdminRole() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("preferred_username", "david"))
                                .authorities(new SimpleGrantedAuthority("ROLE_AUDITOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void transferValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/transactions/transfer")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("preferred_username", "david"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromAccountId": 1,
                                  "toAccountId": 2,
                                  "amount": -5
                                }
                                """))
                .andExpect(status().isBadRequest());

        assertThat(transactionRepository.count()).isZero();
        verifyNoInteractions(accountClient);
    }

    @Test
    void transferPersistsSuccessfulTransactionForAuthorizedUser() throws Exception {
        when(accountClient.getAccount(1L)).thenReturn(accountResponse(1L, "david"));
        when(accountClient.getAccount(2L)).thenReturn(accountResponse(2L, "emma"));

        mockMvc.perform(post("/transactions/transfer")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("preferred_username", "david"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validTransferRequest()))
                .andExpect(status().isOk());

        assertThat(transactionRepository.findAll())
                .singleElement()
                .satisfies(transaction -> {
                    assertThat(transaction.getFromAccountId()).isEqualTo(1L);
                    assertThat(transaction.getToAccountId()).isEqualTo(2L);
                    assertThat(transaction.getAmount()).isEqualByComparingTo("25.00");
                    assertThat(transaction.getStatus()).isEqualTo("SUCCESS");
                });
    }

    private static String validTransferRequest() {
        return """
                {
                  "fromAccountId": 1,
                  "toAccountId": 2,
                  "amount": 25
                }
                """;
    }

    private static AccountResponse accountResponse(Long id, String ownerUsername) {
        return AccountResponse.builder()
                .id(id)
                .ownerUsername(ownerUsername)
                .accountNumber("ACC-" + id)
                .balance(new BigDecimal("100.00"))
                .accountType("SAVINGS")
                .build();
    }
}
