package com.banking.accountservice.service.impl;

import com.banking.accountservice.domain.Account;
import com.banking.accountservice.dto.AccountRequest;
import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.exception.AccessDeniedException;
import com.banking.accountservice.exception.AccountNotFoundException;
import com.banking.accountservice.mapper.AccountMapper;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Cacheable(value = "accounts", key = "#username")
    @Override
    public List<AccountResponse> getAccountsForUser(String username) {
        // check if ADMIN
        if (isAdmin()) {
            return accountRepository.findAll()
                    .stream()
                    .map(AccountMapper::toDto)
                    .toList();
        }

        // USER
        return accountRepository.findByOwnerUsername(username)
                .stream()
                .map(account -> AccountMapper.toDto(account))
                .toList();
    }

    private boolean isAdmin() {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @Cacheable(value = "account", key = "#id")
    @Override
    public AccountResponse getAccount(Long id, String username) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getOwnerUsername().equals(username)) {
            throw new AccessDeniedException("Access denied to this account");
        }

        return AccountMapper.toDto(account);
    }

    // Correct CacheEvict : @CacheEvict(value = "accounts", key = "#request.ownerUsername") for production
    // The below CacheEvict will remove all entries from Cache which is not good for production.
    @Override
    @CacheEvict(value = {"accounts", "account"}, allEntries = true)
    public AccountResponse createAccount(AccountRequest request) {

        Account account = Account.builder()
                .ownerUsername(request.getOwnerUsername())
                .accountNumber(request.getAccountNumber())
                .balance(request.getBalance())
                .accountType(request.getAccountType())
                .build();

        return AccountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @CacheEvict(value = {"accounts", "account"}, allEntries = true)
    public AccountResponse updateAccount(Long id, AccountRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        account.setOwnerUsername(request.getOwnerUsername());
        account.setAccountNumber(request.getAccountNumber());
        account.setBalance(request.getBalance());
        account.setAccountType(request.getAccountType());

        return AccountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @CacheEvict(value = {"accounts", "account"}, allEntries = true)
    public void deleteAccount(Long id) {

        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException("Account not found");
        }

        accountRepository.deleteById(id);
    }
}