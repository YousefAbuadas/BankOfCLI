package com.example.service;

import static com.example.service.BankServiceException.Code.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class Money {
    // Keeps the frontend's dollar-number -> integer-cent conversion within a safe range.
    static final BigDecimal MAX_BALANCE = new BigDecimal("999999999999.99");
    private Money() {}

    static BigDecimal amount(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(MAX_BALANCE) > 0) throw invalidAmount();
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw invalidAmount();
        }
    }

    static void checkCredit(BigDecimal balance, BigDecimal amount) {
        if (balance.add(amount).compareTo(MAX_BALANCE) > 0) throw invalidAmount();
    }

    static void requireUsd(String currency) {
        if (!"USD".equals(currency)) {
            throw new BankServiceException(UNSUPPORTED_CURRENCY, "This operation requires a USD account.");
        }
    }

    private static BankServiceException invalidAmount() {
        return new BankServiceException(INVALID_AMOUNT,
                "Enter a positive dollar amount with at most two decimal places; the balance limit is 999999999999.99.");
    }
}
