package com.example.tinyledger.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record representing a single money movement on an account.
 *
 * <p>Assumptions:
 * <ul>
 *   <li>A DEPOSIT increases the balance; a WITHDRAWAL decreases it.</li>
 *   <li>Amounts are always positive - direction is conveyed by {@link TransactionType}.</li>
 *   <li>{@link BigDecimal} is used for monetary values to avoid floating-point rounding errors.</li>
 *   <li>Each transaction is assigned a random UUID on creation.</li>
 *   <li>Timestamps are recorded in UTC via {@link Instant}.</li>
 * </ul>
 */
public record Transaction(
        String id,
        TransactionType type,
        BigDecimal amount,
        String description,
        Instant timestamp,
        BigDecimal balanceAfter
) {
    /**
     * Factory - auto-generates {@code id} and {@code timestamp}.
     */
    public static Transaction create(
            TransactionType type,
            BigDecimal amount,
            String description,
            BigDecimal balanceAfter) {

        return new Transaction(
                UUID.randomUUID().toString(),
                type,
                amount,
                description,
                Instant.now(),
                balanceAfter
        );
    }
}
