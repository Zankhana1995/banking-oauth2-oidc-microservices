package com.banking.accountservice.repository;

import com.banking.accountservice.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // This method will be used to enforce ownership access.
    List<Account> findByOwnerUsername(String ownerUsername);

}
