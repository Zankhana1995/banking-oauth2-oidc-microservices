package com.banking.apigateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null) {
            return Collections.emptyList();
        }

        Object rolesObj = realmAccess.get("roles");

        if (!(rolesObj instanceof Collection<?> roles)) {
            return Collections.emptyList();
        }

        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Object role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toString()));
        }

        return authorities;
    }
}