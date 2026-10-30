package com.example.controllers;

import com.example.service.BankServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// Turns the service layer's BankServiceException into a proper HTTP status for EVERY controller.
// Without this, insufficient funds, bad amounts, wrong passwords, etc. all came back as 500.
// Body is { "code": "...", "message": "..." } - the frontend's apiClient shows "message" to the user.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BankServiceException.class)
    public ResponseEntity<Map<String, String>> handleBankServiceException(BankServiceException e) {
        HttpStatus status = switch (e.getCode()) {
            case VALIDATION_ERROR, INVALID_AMOUNT, INVALID_DESTINATION, UNSUPPORTED_CURRENCY -> HttpStatus.BAD_REQUEST;
            case INVALID_CREDENTIALS, UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case USER_ID_TAKEN -> HttpStatus.CONFLICT;
            case INSUFFICIENT_FUNDS -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return ResponseEntity.status(status)
                .body(Map.of("code", e.getCode().name(), "message", e.getMessage()));
    }
}
