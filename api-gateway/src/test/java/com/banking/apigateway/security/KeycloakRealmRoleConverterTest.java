package com.banking.apigateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void convertsRealmRolesToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("account.read", "transaction.write")))
                .build();

        List<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities)
                .containsExactlyInAnyOrder("ROLE_account.read", "ROLE_transaction.write");
    }

    @Test
    void returnsEmptyAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user-1")
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void returnsEmptyAuthoritiesWhenRolesClaimIsNotACollection() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", "account.read"))
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }
}
