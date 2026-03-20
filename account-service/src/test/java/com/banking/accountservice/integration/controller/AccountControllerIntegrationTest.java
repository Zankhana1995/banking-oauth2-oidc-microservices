package com.banking.accountservice.integration.controller;

import com.banking.accountservice.AccountServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AccountServiceApplication.class)
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAccountsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAccountsReturnsOnlyCurrentUsersAccounts() throws Exception {
        mockMvc.perform(get("/accounts")
                        .with(jwt().jwt(jwt -> jwt.claim("preferred_username", "david"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].ownerUsername", everyItem(is("david"))));
    }

    @Test
    void getAccountReturnsForbiddenWhenUserDoesNotOwnIt() throws Exception {
        mockMvc.perform(get("/accounts/3")
                        .with(jwt().jwt(jwt -> jwt.claim("preferred_username", "david"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access denied to this account"))
                .andExpect(jsonPath("$.path").value("/accounts/3"));
    }

    @Test
    void createAccountSucceedsForAdminRole() throws Exception {
        String requestBody = """
                {
                  "ownerUsername": "new-user",
                  "accountNumber": "ACC9999",
                  "balance": 1000,
                  "accountType": "SAVINGS"
                }
                """;

        mockMvc.perform(post("/accounts")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim("preferred_username", "admin"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerUsername").value("new-user"))
                .andExpect(jsonPath("$.accountNumber").value("ACC9999"))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));
    }
}
