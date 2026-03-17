package com.banking.accountservice.dto;

import com.banking.accountservice.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {

    @NotBlank
    private String ownerUsername;

    @NotBlank
    private String accountNumber;

    @NotNull
    private BigDecimal balance;

    @NotNull
    private AccountType accountType;
}