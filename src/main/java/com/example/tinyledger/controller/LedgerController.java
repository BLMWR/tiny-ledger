package com.example.tinyledger.controller;

import com.example.tinyledger.model.BalanceResponse;
import com.example.tinyledger.model.MoneyMovementRequest;
import com.example.tinyledger.model.Transaction;
import com.example.tinyledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing ledger operations.
 *
 * <p>All endpoints are scoped under {@code /accounts/{accountId}} so the design
 * naturally supports multiple accounts without any structural change.
 *
 * <p>The seeded account ID is {@code "default"}, so typical calls look like:
 * <pre>
 *   POST /accounts/default/deposits
 *   POST /accounts/default/withdrawals
 *   GET  /accounts/default/balance
 *   GET  /accounts/default/transactions
 * </pre>
 */
@RestController
@RequestMapping("/accounts/{accountId}")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * GET /accounts/{accountId}/balance
     * Returns the current balance.
     */
    @GetMapping("/balance")
    public BalanceResponse getBalance(@PathVariable String accountId) {
        return new BalanceResponse(accountId, ledgerService.getBalance(accountId));
    }

    /**
     * POST /accounts/{accountId}/deposits
     * Records a deposit and returns the resulting transaction.
     */
    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction deposit(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyMovementRequest request) {

        return ledgerService.deposit(accountId, request.amount(), request.description());
    }

    /**
     * POST /accounts/{accountId}/withdrawals
     * Records a withdrawal and returns the resulting transaction.
     */
    @PostMapping("/withdrawals")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction withdraw(
            @PathVariable String accountId,
            @Valid @RequestBody MoneyMovementRequest request) {

        return ledgerService.withdraw(accountId, request.amount(), request.description());
    }

    /**
     * GET /accounts/{accountId}/transactions
     * Returns the full transaction history, oldest first.
     */
    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@PathVariable String accountId) {
        return ledgerService.getTransactions(accountId);
    }
}
