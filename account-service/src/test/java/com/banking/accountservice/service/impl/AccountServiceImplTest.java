package com.banking.accountservice.service.impl;

import com.banking.accountservice.domain.Account;
import com.banking.accountservice.domain.AccountType;
import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.exception.AccessDeniedException;
import com.banking.accountservice.exception.InsufficientBalanceException;
import com.banking.accountservice.repository.AccountRepository;
import com.banking.accountservice.security.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void getAccountsForUserReturnsAllAccountsWhenCallerCanViewEverything() {
        List<Account> accounts = List.of(
                account(1L, "david", "ACC1001", "5000.00"),
                account(2L, "emma", "ACC2001", "12000.00")
        );

        when(authorizationService.canViewAllAccounts()).thenReturn(true);
        when(accountRepository.findAll()).thenReturn(accounts);

        List<AccountResponse> response = accountService.getAccountsForUser("ignored");

        assertThat(response)
                .hasSize(2)
                .extracting(AccountResponse::getOwnerUsername)
                .containsExactly("david", "emma");
        verify(accountRepository).findAll();
        verify(accountRepository, never()).findByOwnerUsername(any());
    }

    @Test
    void getAccountThrowsWhenCallerCannotAccessIt() {
        Account account = account(3L, "emma", "ACC2001", "12000.00");

        when(accountRepository.findById(3L)).thenReturn(Optional.of(account));
        when(authorizationService.canAccessAccount(account)).thenReturn(false);

        assertThatThrownBy(() -> accountService.getAccount(3L, "david"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access denied to this account");
    }

    @Test
    void debitThrowsWhenBalanceIsTooLow() {
        Account account = account(1L, "david", "ACC1001", "50.00");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(authorizationService.canAccessAccount(account)).thenReturn(true);

        assertThatThrownBy(() -> accountService.debit(1L, new BigDecimal("60.00")))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient balance");

        assertThat(account.getBalance()).isEqualByComparingTo("50.00");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void creditAddsAmountAndPersistsUpdatedAccount() {
        Account account = account(1L, "david", "ACC1001", "100.00");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(authorizationService.canAccessAccount(account)).thenReturn(true);
        when(accountRepository.save(account)).thenReturn(account);

        AccountResponse response = accountService.credit(1L, new BigDecimal("25.00"));

        assertThat(response.getBalance()).isEqualByComparingTo("125.00");
        assertThat(account.getBalance()).isEqualByComparingTo("125.00");
        verify(accountRepository).save(account);
    }

    private static Account account(Long id, String owner, String accountNumber, String balance) {
        return Account.builder()
                .id(id)
                .ownerUsername(owner)
                .accountNumber(accountNumber)
                .balance(new BigDecimal(balance))
                .accountType(AccountType.SAVINGS)
                .build();
    }
}
