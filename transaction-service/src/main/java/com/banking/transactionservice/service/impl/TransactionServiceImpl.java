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
        // TEMP: hardcoded token (next step we fix this)
        String token = "PASTE_TOKEN_HERE";

        accountClient.validateAccount(request.getFromAccountId(), token);
        accountClient.validateAccount(request.getToAccountId(), token);

        // next: debit + credit logic
    }
}
