package com.example.BankofCLI.models;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String accountId;
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    private double amount;
    private String date; // ISO-8601 instant, e.g. 2026-10-23T15:00:00Z
    private String description;

    public Transaction() {}

    public Transaction(String accountId, String type, double amount, String description) {
        this(accountId, type, amount, Instant.now(), description);
    }

    // Used by JpaTransactionStore: both halves of a transfer share the same timestamp
    public Transaction(String accountId, String type, double amount, Instant date, String description) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.date = date.toString();
        this.description = description;
    }

    // Getters
    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getDescription() { return description; }
}
