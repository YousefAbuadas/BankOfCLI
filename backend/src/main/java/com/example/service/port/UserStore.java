package com.example.service.port;

import com.example.service.port.BankData.UserData;
import java.util.Optional;

/** Implement using the team's repository, participating in the service's Spring transaction. */
public interface UserStore {
    Optional<UserData> findById(String userId);
    Optional<UserData> findByEmail(String normalizedEmail);

    /**
     * Persist a new USER, assign its ID and return its saved values.
     * Enforce normalized-email uniqueness in the database, including concurrent inserts.
     * Translate only duplicate-email failures to DuplicateEmailException.
     */
    UserData create(String firstName, String lastName, String normalizedEmail, String passwordHash);

    class DuplicateEmailException extends RuntimeException {
        public DuplicateEmailException(Throwable cause) { super("Email already exists.", cause); }
    }
}
