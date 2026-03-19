package com.banking.transactionservice.service.impl;

import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.domain.Transaction;
import com.banking.transactionservice.dto.AccountResponse;
import com.banking.transactionservice.repository.TransactionRepository;
import com.banking.transactionservice.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountClient accountClient;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, String username) {

        AccountResponse fromAccount = accountClient.getAccount(fromId);
        AccountResponse toAccount = accountClient.getAccount(toId);

        validateOwnership(fromAccount, username);

        boolean debitSuccess = false;

        try {
            accountClient.debit(fromId, amount);
            debitSuccess = true;

            accountClient.credit(toId, amount);

            saveTransaction(fromId, toId, amount, "SUCCESS");

        } catch (Exception ex) {

            if (debitSuccess) {
                rollbackDebit(fromId, amount);
            }

            saveTransaction(fromId, toId, amount, "FAILED");

            throw new RuntimeException("Transfer failed", ex);
        }
    }

    private void validateOwnership(AccountResponse account, String username) {
        if (!account.getOwnerUsername().equals(username)) {
            throw new RuntimeException("You can only transfer from your own account");
        }
    }

    private void rollbackDebit(Long fromId, BigDecimal amount) {
        try {
            accountClient.credit(fromId, amount);
        } catch (Exception ex) {
            // log later (keep simple now)
        }
    }

    private void saveTransaction(Long fromId, Long toId, BigDecimal amount, String status) {

        Transaction txn = Transaction.builder()
                .fromAccountId(fromId)
                .toAccountId(toId)
                .amount(amount)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(txn);
    }
}
