package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountRequest;
import com.banking.accountservice.dto.AccountResponse;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    List<AccountResponse> getAccountsForUser(String username);

    AccountResponse getAccount(Long id, String username);

    AccountResponse createAccount(AccountRequest request);

    AccountResponse updateAccount(Long id, AccountRequest request);

    void deleteAccount(Long id);

    AccountResponse debit(Long id, BigDecimal amount);

    AccountResponse credit(Long id, BigDecimal amount);
}