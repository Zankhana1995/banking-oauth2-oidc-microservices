package com.banking.accountservice.dto;

import com.banking.accountservice.domain.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountType;
    private String ownerUsername;
}
