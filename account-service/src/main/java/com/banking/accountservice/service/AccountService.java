package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;

import java.util.List;

public interface AccountService {

    List<AccountResponse> getAccountsForUser(String username);

    AccountResponse getAccount(Long id, String username);
}
