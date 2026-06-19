package com.example.tinyledger.service;

import com.example.tinyledger.exception.AccountNotFoundException;
import com.example.tinyledger.exception.InsufficientFundsException;
import com.example.tinyledger.model.Account;
import com.example.tinyledger.model.Transaction;
import com.example.tinyledger.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core ledger service backed entirely by in-memory data structures.
 *
 * <p>Design decisions / assumptions:
 * <ul>
 *   <li>A single {@code "default"} account is seeded at startup. Creating accounts is
 *       out of scope, but the underlying map easily supports multiple accounts.</li>
 *   <li>Mutations are wrapped in a {@code synchronized} block on the account object to
 *       prevent races (e.g. two concurrent withdrawals both passing the balance check).</li>
 *   <li>Amounts must be strictly positive; direction is expressed by the endpoint called.</li>
 *   <li>Negative balances are not permitted.</li>
 * </ul>
 */
@Service
public class LedgerService {

    public static final String DEFAULT_ACCOUNT_ID = "default";

    /** In-memory store: accountId → Account. */
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public LedgerService() {
        accounts.put(DEFAULT_ACCOUNT_ID, new Account(DEFAULT_ACCOUNT_ID));
    }


    public BigDecimal getBalance(String accountId) {
        return findAccount(accountId).getBalance();
    }


    public Transaction deposit(String accountId, BigDecimal amount, String description) {
        validatePositiveAmount(amount);
        Account account = findAccount(accountId);

        synchronized (account) {
            BigDecimal newBalance = account.getBalance().add(amount);
            account.setBalance(newBalance);

            Transaction tx = Transaction.create(TransactionType.DEPOSIT, amount, description, newBalance);
            account.addTransaction(tx);
            return tx;
        }
    }

    public Transaction withdraw(String accountId, BigDecimal amount, String description) {
        validatePositiveAmount(amount);
        Account account = findAccount(accountId);

        synchronized (account) {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds: current balance is " + account.getBalance()
                                + ", requested withdrawal is " + amount + ".");
            }

            BigDecimal newBalance = account.getBalance().subtract(amount);
            account.setBalance(newBalance);

            Transaction tx = Transaction.create(TransactionType.WITHDRAWAL, amount, description, newBalance);
            account.addTransaction(tx);
            return tx;
        }
    }

    public List<Transaction> getTransactions(String accountId) {
        return findAccount(accountId).getTransactions();
    }

    private Account findAccount(String accountId) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}
