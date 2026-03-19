package com.banking.transactionservice.controller;

import com.banking.transactionservice.dto.TransferRequest;
import com.banking.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("preferred_username");

        transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                username
        );

        return ResponseEntity.ok().build();
    }
}