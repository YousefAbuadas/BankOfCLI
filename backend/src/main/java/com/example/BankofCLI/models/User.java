package com.example.BankofCLI.models;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String firstName;

    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    // "USER" or "ADMIN" - the service layer's UserData/UserIdentity carry this
    @Column(nullable = false)
    private String role = "USER";

    public User() {}

    // Used by registration (JpaUserStore)
    public User(String firstName, String lastName, String email, String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Kept so existing callers (e.g. TransactionIntegrationTest) still compile:
    // a single display name is stored as the first name
    public User(String name, String email, String passwordHash) {
        this(name, "", email, passwordHash);
    }

    // Getters & Setters
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getName() { return (firstName + " " + lastName).trim(); }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
