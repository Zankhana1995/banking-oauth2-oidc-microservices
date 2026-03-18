package com.banking.transactionservice.service.impl;

import com.banking.transactionservice.client.AccountClient;
import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountClient accountClient;

    @Override
    public void transfer(TransferRequest request) {

        // Step 1: Validate both accounts via account-service
        accountClient.validateAccount(request.getFromAccountId());
        accountClient.validateAccount(request.getToAccountId());

        // Step 2: (Next step) debit + credit logic

    }
}
