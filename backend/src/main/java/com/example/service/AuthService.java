package com.example.service;

import com.example.service.contract.BankContracts.*;
import com.example.service.port.*;
import com.example.service.port.BankData.UserData;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;
import static com.example.service.BankServiceException.Code.*;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserStore users;
    private final AccountStore accounts;
    private final PasswordEncoder passwords;
    private final TokenIssuer tokens;
    private final String dummyHash;

    public AuthService(UserStore users, AccountStore accounts, PasswordEncoder passwords, TokenIssuer tokens) {
        this.users = users;
        this.accounts = accounts;
        this.passwords = passwords;
        this.tokens = tokens;
        this.dummyHash = passwords.encode("unused-login-timing-password");
    }

    /** User, checking account and savings account commit together, or all roll back. */
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse register(RegisterRequest request) {
        if (request == null) throw validation("Registration details are required.");
        String firstName = name(request.firstName(), "First name");
        String lastName = name(request.lastName(), "Last name");
        String email = email(request.email());
        if (!validPassword(request.password())) {
            throw validation("Password must be nonblank and at most 72 UTF-8 bytes.");
        }
        if (users.findByEmail(email).isPresent()) throw duplicateEmail();

        UserData user;
        try {
            user = users.create(firstName, lastName, email, passwords.encode(request.password()));
        } catch (UserStore.DuplicateEmailException exception) {
            // The adapter must enforce a unique database constraint for concurrent registrations.
            throw duplicateEmail();
        }

        accounts.create(user.userId(), "Everyday Checking", AccountType.CHECKING);
        accounts.create(user.userId(), "Personal Savings", AccountType.SAVINGS);
        return response(user);
    }

    /** Passwords are matched against hashes; this service keeps no logged-in-user state. */
    public AuthResponse login(LoginRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()
                || request.email().strip().length() > 254 || !validPassword(request.password())) {
            throw invalidCredentials();
        }
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        UserData user = users.findByEmail(email).orElse(null);
        boolean matches = passwords.matches(request.password(), user == null ? dummyHash : user.passwordHash());
        if (user == null || !matches) throw invalidCredentials();
        return response(user);
    }

    private AuthResponse response(UserData user) {
        String token = tokens.generateToken(ServiceMapper.identity(user));
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("The TokenIssuer did not return a token.");
        }
        return new AuthResponse(user.userId(), user.firstName(), user.lastName(), user.email(), token);
    }

    private static String name(String value, String label) {
        if (value == null || value.isBlank() || value.strip().length() > 60) {
            throw validation(label + " is required and must be at most 60 characters.");
        }
        return value.strip();
    }

    private static String email(String value) {
        if (value == null || value.strip().length() > 254 || !EMAIL.matcher(value.strip()).matches()) {
            throw validation("Enter a valid email address.");
        }
        return value.strip().toLowerCase(Locale.ROOT);
    }

    private static boolean validPassword(String value) {
        return value != null && !value.isBlank() && value.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private static BankServiceException validation(String message) {
        return new BankServiceException(VALIDATION_ERROR, message);
    }
    private static BankServiceException duplicateEmail() {
        return new BankServiceException(USER_ID_TAKEN, "That email address is already registered.");
    }
    private static BankServiceException invalidCredentials() {
        return new BankServiceException(INVALID_CREDENTIALS, "Email or password is incorrect.");
    }
}
