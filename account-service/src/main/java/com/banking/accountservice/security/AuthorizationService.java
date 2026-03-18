package com.banking.accountservice.security;

import com.banking.accountservice.domain.Account;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {

    public boolean canAccessAccount(Account account) {

        String username = getUsername();

        return isAdmin() || isServiceAccount() || account.getOwnerUsername().equals(username);
    }

    public boolean canViewAllAccounts() {
        return isAdmin() || isServiceAccount();
    }

    private boolean isAdmin() {
        return hasRole();
    }

    private boolean isServiceAccount() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof Jwt jwt) {
            String clientId = jwt.getClaimAsString("client_id");
            return "transaction-client".equals(clientId);
        }

        return false;
    }

    private boolean hasRole() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getUsername() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("preferred_username");
        }

        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }
}