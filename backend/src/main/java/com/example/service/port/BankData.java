package com.example.service.port;

import com.example.service.contract.BankContracts.AccountType;
import java.math.BigDecimal;
import java.time.Instant;

/** Immutable values exchanged with repository adapters; these are not JPA models. */
public final class BankData {
    private BankData() {}

    public record UserData(String userId, String firstName, String lastName, String email,
                           String passwordHash, String role) {
        @Override public String toString() { return "UserData[credentials redacted]"; }
    }

    public record AccountData(String accountId, String userId, String name, AccountType type,
                              BigDecimal balance, String currency) {}

    public record TransactionData(String transactionId, String accountId, String type,
                                  BigDecimal amount, Instant date, String description) {}
}
