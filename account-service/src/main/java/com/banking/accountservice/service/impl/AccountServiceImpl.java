package com.banking.accountservice.service.impl;

import com.banking.accountservice.domain.Account;
import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.mapper.AccountMapper;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountResponse> getAccountsForUser(String username) {

        return accountRepository.findByOwnerUsername(username)
                .stream()
                .map(account -> AccountMapper.toDto(account))
                .toList();
    }

    @Override
    public AccountResponse getAccount(Long id, String username) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getOwnerUsername().equals(username)) {
            throw new RuntimeException("Access denied to this account");
        }

        return AccountMapper.toDto(account);
    }
}