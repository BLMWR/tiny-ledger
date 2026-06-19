package com.example.tinyledger;

import com.example.tinyledger.exception.InsufficientFundsException;
import com.example.tinyledger.model.Transaction;
import com.example.tinyledger.model.TransactionType;
import com.example.tinyledger.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LedgerServiceTest {

    private LedgerService service;
    private static final String ACCOUNT = LedgerService.DEFAULT_ACCOUNT_ID;

    @BeforeEach
    void setUp() {
        service = new LedgerService();
    }

    @Test
    void shouldReturnZeroBalance_whenAccountIsCreated() {
        assertThat(service.getBalance(ACCOUNT)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldIncreaseBalance_whenDepositIsMade() {
        service.deposit(ACCOUNT, new BigDecimal("100.00"), "salary");

        assertThat(service.getBalance(ACCOUNT)).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldReturnTransactionWithCorrectFields_whenDepositIsMade() {
        Transaction tx = service.deposit(ACCOUNT, new BigDecimal("50.00"), "gift");

        assertThat(tx.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.amount()).isEqualByComparingTo("50.00");
        assertThat(tx.balanceAfter()).isEqualByComparingTo("50.00");
        assertThat(tx.description()).isEqualTo("gift");
        assertThat(tx.id()).isNotBlank();
        assertThat(tx.timestamp()).isNotNull();
    }

    @Test
    void shouldDecreaseBalance_whenWithdrawalIsMade() {
        service.deposit(ACCOUNT, new BigDecimal("200.00"), "");
        service.withdraw(ACCOUNT, new BigDecimal("75.00"), "rent");

        assertThat(service.getBalance(ACCOUNT)).isEqualByComparingTo("125.00");
    }

    @Test
    void shouldReturnTransactionWithCorrectFields_whenWithdrawalIsMade() {
        service.deposit(ACCOUNT, new BigDecimal("200.00"), "");
        Transaction tx = service.withdraw(ACCOUNT, new BigDecimal("80.00"), "shopping");

        assertThat(tx.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.amount()).isEqualByComparingTo("80.00");
        assertThat(tx.balanceAfter()).isEqualByComparingTo("120.00");
    }

    @Test
    void shouldThrowInsufficientFunds_whenWithdrawalExceedsBalance() {
        service.deposit(ACCOUNT, new BigDecimal("10.00"), "");

        assertThatThrownBy(() -> service.withdraw(ACCOUNT, new BigDecimal("10.01"), ""))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void shouldLeaveZeroBalance_whenWithdrawalEqualsExactBalance() {
        service.deposit(ACCOUNT, new BigDecimal("50.00"), "");
        service.withdraw(ACCOUNT, new BigDecimal("50.00"), "");

        assertThat(service.getBalance(ACCOUNT)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnTransactionsInChronologicalOrder_whenHistoryIsRequested() {
        service.deposit(ACCOUNT, new BigDecimal("100.00"), "first");
        service.deposit(ACCOUNT, new BigDecimal("50.00"), "second");
        service.withdraw(ACCOUNT, new BigDecimal("30.00"), "third");

        List<Transaction> history = service.getTransactions(ACCOUNT);

        assertThat(history).hasSize(3);
        assertThat(history.get(0).description()).isEqualTo("first");
        assertThat(history.get(1).description()).isEqualTo("second");
        assertThat(history.get(2).description()).isEqualTo("third");
    }

    @Test
    void shouldThrowIllegalArgument_whenDepositAmountIsZero() {
        assertThatThrownBy(() -> service.deposit(ACCOUNT, BigDecimal.ZERO, ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowIllegalArgument_whenDepositAmountIsNegative() {
        assertThatThrownBy(() -> service.deposit(ACCOUNT, new BigDecimal("-1"), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
