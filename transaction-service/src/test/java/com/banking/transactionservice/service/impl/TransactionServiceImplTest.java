package com.banking.transactionservice.service.impl;

import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.domain.Transaction;
import com.banking.transactionservice.dto.AccountResponse;
import com.banking.transactionservice.repository.TransactionRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private TransactionRepository transactionRepository;

    @Test
    void transferSavesSuccessfulTransactionAndIncrementsSuccessCounter() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TransactionServiceImpl service = new TransactionServiceImpl(accountClient, transactionRepository, meterRegistry);

        when(accountClient.getAccount(1L)).thenReturn(accountResponse(1L, "david"));
        when(accountClient.getAccount(2L)).thenReturn(accountResponse(2L, "emma"));

        service.transfer(1L, 2L, new BigDecimal("25.00"), "david");

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(accountClient).debit(1L, new BigDecimal("25.00"));
        verify(accountClient).credit(2L, new BigDecimal("25.00"));

        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getFromAccountId()).isEqualTo(1L);
        assertThat(saved.getToAccountId()).isEqualTo(2L);
        assertThat(saved.getAmount()).isEqualByComparingTo("25.00");
        assertThat(saved.getStatus()).isEqualTo("SUCCESS");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(meterRegistry.counter("transactions.success").count()).isEqualTo(1.0);
        assertThat(meterRegistry.counter("transactions.failure").count()).isZero();
    }

    @Test
    void transferRejectsRequestsFromAnotherUsersAccount() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TransactionServiceImpl service = new TransactionServiceImpl(accountClient, transactionRepository, meterRegistry);

        when(accountClient.getAccount(1L)).thenReturn(accountResponse(1L, "emma"));
        when(accountClient.getAccount(2L)).thenReturn(accountResponse(2L, "david"));

        assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("10.00"), "david"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You can only transfer from your own account");

        verify(accountClient, never()).debit(any(), any());
        verify(transactionRepository, never()).save(any(Transaction.class));
        assertThat(meterRegistry.counter("transactions.success").count()).isZero();
        assertThat(meterRegistry.counter("transactions.failure").count()).isZero();
    }

    @Test
    void transferRollsBackDebitAndMarksFailureWhenCreditFails() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        TransactionServiceImpl service = new TransactionServiceImpl(accountClient, transactionRepository, meterRegistry);

        when(accountClient.getAccount(1L)).thenReturn(accountResponse(1L, "david"));
        when(accountClient.getAccount(2L)).thenReturn(accountResponse(2L, "emma"));
        doThrow(new RuntimeException("credit failed")).when(accountClient).credit(2L, new BigDecimal("40.00"));

        assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("40.00"), "david"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Transfer failed")
                .hasCauseInstanceOf(RuntimeException.class);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(accountClient).debit(1L, new BigDecimal("40.00"));
        verify(accountClient).credit(2L, new BigDecimal("40.00"));
        verify(accountClient).credit(1L, new BigDecimal("40.00"));

        Transaction saved = transactionCaptor.getValue();
        assertThat(saved.getStatus()).isEqualTo("FAILED");
        assertThat(meterRegistry.counter("transactions.success").count()).isZero();
        assertThat(meterRegistry.counter("transactions.failure").count()).isEqualTo(1.0);
    }

    private static AccountResponse accountResponse(Long id, String ownerUsername) {
        return AccountResponse.builder()
                .id(id)
                .ownerUsername(ownerUsername)
                .accountNumber("ACC-" + id)
                .balance(new BigDecimal("100.00"))
                .accountType("SAVINGS")
                .build();
    }
}
