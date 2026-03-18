package com.banking.transactionservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.keycloak")
@Data
public class KeycloakProperties {

    private String tokenUrl;
    private String clientId;
    private String clientSecret;
}