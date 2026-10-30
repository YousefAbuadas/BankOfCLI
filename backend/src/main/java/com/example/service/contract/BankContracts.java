package com.example.service.contract;

import java.math.BigDecimal;

/** Service request/response values, not database entities. Wire amounts are dollars. */
public final class BankContracts {
    private BankContracts() {}

    public enum AccountType { CHECKING, SAVINGS }

    public record RegisterRequest(String firstName, String lastName, String email, String password) {
        @Override public String toString() { return "RegisterRequest[credentials redacted]"; }
    }

    public record LoginRequest(String email, String password) {
        @Override public String toString() { return "LoginRequest[credentials redacted]"; }
    }

    /** Password-free identity passed to the team's JWT implementation. */
    public record UserIdentity(String userId, String firstName, String lastName, String email, String role) {}

    public record AuthResponse(String userId, String firstName, String lastName, String email, String token) {
        @Override public String toString() { return "AuthResponse[token redacted]"; }
    }

    public record UserReference(String userId) {}

    public record AccountResponse(String accountId, UserReference user, String currencyType,
                                  BigDecimal accountBalance, AccountType type, String name) {}

    public record AccountReference(String accountId) {}

    public record TransactionResponse(String transactionId, AccountReference account, String type,
                                      BigDecimal amount, String date, String description) {}

    public record OperationResponse(boolean success) {}
}
