package com.example.tinyledger.model;

import java.math.BigDecimal;

public record BalanceResponse(String accountId, BigDecimal balance) {}
