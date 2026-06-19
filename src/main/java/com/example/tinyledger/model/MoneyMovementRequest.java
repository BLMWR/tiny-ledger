package com.example.tinyledger.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request body for deposit and withdrawal endpoints.
 *
 * <p>{@code description} is optional - omitting it defaults to an empty string.
 */
public record MoneyMovementRequest(

        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
        BigDecimal amount,

        String description
) {
    /** Compact constructor - normalise null description. */
    public MoneyMovementRequest {
        if (description == null) description = "";
    }
}
