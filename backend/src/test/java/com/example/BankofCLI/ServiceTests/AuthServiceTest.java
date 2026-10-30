package com.example.BankofCLI.ServiceTests;

import com.example.service.AuthService;
import com.example.service.BankServiceException;
import com.example.service.contract.BankContracts.*;
import com.example.service.port.AccountStore;
import com.example.service.port.TokenIssuer;
import com.example.service.port.UserStore;
import com.example.service.port.BankData.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.example.service.BankServiceException.Code.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserStore users;
    @Mock AccountStore accounts;
    @Mock PasswordEncoder passwords;
    @Mock TokenIssuer tokens;
    AuthService service;

    @BeforeEach
    void setUp() {
        when(passwords.encode("unused-login-timing-password")).thenReturn("dummy-hash");
        service = new AuthService(users, accounts, passwords, tokens);
    }

    @Test
    void registerNormalizesInputCreatesBothAccountsAndIssuesToken() {
        UserData saved = new UserData("user-1", "Ada", "Lovelace", "ada@example.com", "hash", "USER");
        when(passwords.encode("secret")).thenReturn("hash");
        when(users.create("Ada", "Lovelace", "ada@example.com", "hash")).thenReturn(saved);
        when(tokens.generateToken(any(UserIdentity.class))).thenReturn("signed-token");

        AuthResponse response = service.register(new RegisterRequest(" Ada ", " Lovelace ", "ADA@EXAMPLE.COM", "secret"));

        assertEquals("user-1", response.userId());
        assertEquals("signed-token", response.token());
        verify(users).findByEmail("ada@example.com");
        verify(accounts).create("user-1", "Everyday Checking", AccountType.CHECKING);
        verify(accounts).create("user-1", "Personal Savings", AccountType.SAVINGS);
        verify(tokens).generateToken(new UserIdentity("user-1", "Ada", "Lovelace", "ada@example.com", "USER"));
    }

    @Test
    void duplicateEmailDoesNotCreateUserOrAccounts() {
        when(users.findByEmail("ada@example.com"))
                .thenReturn(Optional.of(new UserData("user-1", "Ada", "Lovelace", "ada@example.com", "hash", "USER")));

        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.register(new RegisterRequest("Ada", "Lovelace", "ada@example.com", "secret")));

        assertEquals(USER_ID_TAKEN, error.getCode());
        verify(users, never()).create(anyString(), anyString(), anyString(), anyString());
        verifyNoInteractions(accounts, tokens);
    }

    @Test
    void loginChecksPasswordHashAndIssuesToken() {
        UserData saved = new UserData("user-1", "Ada", "Lovelace", "ada@example.com", "stored-hash", "USER");
        when(users.findByEmail("ada@example.com")).thenReturn(Optional.of(saved));
        when(passwords.matches("secret", "stored-hash")).thenReturn(true);
        when(tokens.generateToken(any(UserIdentity.class))).thenReturn("signed-token");

        AuthResponse response = service.login(new LoginRequest(" ADA@EXAMPLE.COM ", "secret"));

        assertEquals("signed-token", response.token());
        verify(passwords).matches("secret", "stored-hash");
    }

    @Test
    void unknownEmailUsesDummyHashAndRejectsLogin() {
        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.login(new LoginRequest("missing@example.com", "secret")));

        assertEquals(INVALID_CREDENTIALS, error.getCode());
        verify(passwords).matches("secret", "dummy-hash");
        verifyNoInteractions(tokens);
    }
}
