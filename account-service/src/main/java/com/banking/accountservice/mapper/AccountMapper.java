package com.banking.accountservice.mapper;

import com.banking.accountservice.domain.Account;
import com.banking.accountservice.dto.AccountResponse;

public class AccountMapper {

    public static AccountResponse toDto(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .build();
    }
}
