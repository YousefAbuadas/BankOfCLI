package com.example.service;

/** An unchecked exception so a rejected operation rolls back its Spring transaction. */
public final class BankServiceException extends RuntimeException {
    public enum Code {
        VALIDATION_ERROR, INVALID_CREDENTIALS, USER_ID_TAKEN, UNAUTHENTICATED,
        ACCOUNT_NOT_FOUND, INVALID_AMOUNT, INSUFFICIENT_FUNDS,
        INVALID_DESTINATION, UNSUPPORTED_CURRENCY
    }

    private final Code code;

    public BankServiceException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code getCode() { return code; }
}
