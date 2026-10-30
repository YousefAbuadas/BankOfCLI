package com.example.BankofCLI.IntegrationTests;

import com.example.BankofCLI.BankofCliApplication;
import com.example.BankofCLI.models.Account;
import com.example.BankofCLI.models.Transaction;
import com.example.BankofCLI.models.User;
import com.example.BankofCLI.repositories.AccountRepository;
import com.example.BankofCLI.repositories.TransactionRepository;
import com.example.BankofCLI.repositories.UserRepository;
import com.example.JWT.AuthUser;
import com.example.service.BankServiceException;
import com.example.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.service.BankServiceException.Code.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Real services and repositories using application-test.properties.
 * Requires the application's persistence and authenticated-user adapters to be wired.
 * No test transaction surrounds these methods: service transactions must commit or roll back themselves.
 */
@SpringBootTest(classes = BankofCliApplication.class)
@ActiveProfiles("test")
class TransactionIntegrationTest {
    @Autowired TransactionService service;
    @Autowired AccountRepository accounts;
    @Autowired TransactionRepository transactions;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    private final List<String> accountIds = new ArrayList<>();
    private final List<String> userIds = new ArrayList<>();
    private Account checking;
    private Account savings;
    private Account foreignAccount;

    @BeforeEach
    void setUp() {
        User owner = createUser("Owner");
        User otherOwner = createUser("Other Owner");
        checking = createAccount(owner, "Everyday Checking", 100.00, "1234");
        savings = createAccount(owner, "Personal Savings", 50.00, "5678");
        foreignAccount = createAccount(otherOwner, "Other Checking", 200.00, "9012");

        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(owner.getId(), owner.getEmail()), null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        // Delete only this test's fixtures, with history removed before its accounts.
        transactions.deleteAll(transactions.findAll().stream()
                .filter(entry -> accountIds.contains(entry.getAccountId())).toList());
        accounts.deleteAllById(accountIds);
        users.deleteAllById(userIds);
    }

    @Test
    void depositPersistsIncreasedBalanceAndHistory() {
        assertTrue(service.deposit(checking.getId(), new BigDecimal("25.00")).success());

        assertBalance(checking, "125.00");
        assertHistory(checking, "DEPOSIT", "25.00");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-25.00", "1.001"})
    void invalidDepositIsRejectedWithoutChangingBalanceOrHistory(String amount) {
        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.deposit(checking.getId(), amount == null ? null : new BigDecimal(amount)));

        assertEquals(INVALID_AMOUNT, error.getCode());
        assertBalance(checking, "100.00");
        assertTrue(history(checking).isEmpty());
    }

    @Test
    void withdrawalPersistsReducedBalanceAndHistory() {
        assertTrue(service.withdraw(checking.getId(), new BigDecimal("25.00")).success());

        assertBalance(checking, "75.00");
        assertHistory(checking, "WITHDRAWAL", "25.00");
    }

    @Test
    void overdraftIsRejectedWithoutChangingBalanceOrHistory() {
        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.withdraw(checking.getId(), new BigDecimal("100.01")));

        assertEquals(INSUFFICIENT_FUNDS, error.getCode());
        assertBalance(checking, "100.00");
        assertTrue(history(checking).isEmpty());
    }

    @Test
    void transferPersistsBothBalancesAndBothHistoryEntries() {
        assertTrue(service.transfer(checking.getId(), savings.getId(), new BigDecimal("25.00")).success());

        assertBalance(checking, "75.00");
        assertBalance(savings, "75.00");
        assertHistory(checking, "TRANSFER_OUT", "25.00");
        assertHistory(savings, "TRANSFER_IN", "25.00");
    }

    @Test
    void transferWithInsufficientFundsLeavesBothAccountsUnchanged() {
        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.transfer(checking.getId(), savings.getId(), new BigDecimal("100.01")));

        assertEquals(INSUFFICIENT_FUNDS, error.getCode());
        assertBalance(checking, "100.00");
        assertBalance(savings, "50.00");
        assertTrue(history(checking).isEmpty());
        assertTrue(history(savings).isEmpty());
    }

    @Test
    void transferToAnotherCustomersAccountPersistsBothBalancesAndHistory() {
        assertTrue(service.transfer(checking.getId(), foreignAccount.getId(), new BigDecimal("25.00")).success());

        assertBalance(checking, "75.00");
        assertBalance(foreignAccount, "225.00");
        assertHistory(checking, "TRANSFER_OUT", "25.00");
        assertHistory(foreignAccount, "TRANSFER_IN", "25.00");
    }

    @Test
    void transferToSameAccountIsRejected() {
        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.transfer(checking.getId(), checking.getId(), new BigDecimal("25.00")));

        assertEquals(INVALID_DESTINATION, error.getCode());
        assertBalance(checking, "100.00");
        assertTrue(history(checking).isEmpty());
    }

    private User createUser(String name) {
        User user = users.saveAndFlush(new User(name, UUID.randomUUID() + "@example.com",
                passwords.encode("IntegrationPassword123!")));
        userIds.add(user.getId());
        return user;
    }

    private Account createAccount(User owner, String name, double balance, String mask) {
        Account account = accounts.saveAndFlush(new Account(owner.getId(), name, balance, mask));
        accountIds.add(account.getId());
        return account;
    }

    private void assertBalance(Account account, String expected) {
        Account reloaded = accounts.findById(account.getId()).orElseThrow();
        assertEquals(0, new BigDecimal(expected).compareTo(BigDecimal.valueOf(reloaded.getBalance())));
    }

    private List<Transaction> history(Account account) {
        return transactions.findAll().stream()
                .filter(entry -> account.getId().equals(entry.getAccountId())).toList();
    }

    private void assertHistory(Account account, String type, String amount) {
        List<Transaction> entries = history(account);
        assertEquals(1, entries.size());
        assertEquals(type, entries.getFirst().getType());
        assertEquals(0, new BigDecimal(amount).compareTo(BigDecimal.valueOf(entries.getFirst().getAmount())));
    }
}
