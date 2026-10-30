package com.example.BankofCLI.models;

import com.example.service.contract.BankContracts.AccountType;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String ownerId;

    private String name;
    private double balance;
    private String currency = "USD";

    private String mask;

    // CHECKING or SAVINGS - AccountService sorts and the frontend labels accounts by this
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type = AccountType.CHECKING;

    public Account() {}

    public Account(String ownerId, String name, double balance, String mask) {
        this.ownerId = ownerId;
        this.name = name;
        this.balance = balance;
        this.mask = mask;
    }

    // Used by registration (JpaAccountStore.create): new accounts start at $0.00
    public Account(String ownerId, String name, AccountType type) {
        this.ownerId = ownerId;
        this.name = name;
        this.type = type;
        this.balance = 0.0;
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getCurrency() { return currency; }
    public String getMask() { return mask; }
    public AccountType getType() { return type; }
}
