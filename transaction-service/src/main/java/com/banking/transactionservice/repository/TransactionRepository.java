package com.banking.transactionservice.repository;

import com.banking.transactionservice.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
