package com.banking.transactionservice.service;

import java.math.BigDecimal;

public interface TransactionService {

    void transfer(Long fromId, Long toId, BigDecimal amount, String username);

}
