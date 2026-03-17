package com.banking.transactionservice.service;

import com.banking.transactionservice.dto.TransferRequest;

public interface TransactionService {

    void transfer(TransferRequest request);
}
