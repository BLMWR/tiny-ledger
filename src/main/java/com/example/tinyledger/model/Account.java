package com.example.tinyledger.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a ledger account.
 *
 * <p>Assumptions:
 * <ul>
 *   <li>A single "default" account is seeded at startup.</li>
 *   <li>The opening balance is zero.</li>
 *   <li>Withdrawals that would take the balance below zero are rejected.</li>
 * </ul>
 *
 * <p>Thread-safety: mutations are synchronised in {@link com.example.tinyledger.service.LedgerService}
 * on the account instance, so this class stays intentionally simple.
 */
public class Account {

    private final String id;
    private BigDecimal balance;
    private final List<Transaction> transactions;

    public Account(String id) {
        this.id = id;
        this.balance = BigDecimal.ZERO;
        this.transactions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /** Returns an unmodifiable view - callers must not mutate the list directly. */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }
}
