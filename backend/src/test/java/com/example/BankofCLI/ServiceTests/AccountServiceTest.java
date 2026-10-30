package com.example.BankofCLI.ServiceTests;

import com.example.service.AccountService;
import com.example.service.BankServiceException;
import com.example.service.CurrentUserService;
import com.example.service.contract.BankContracts.AccountType;
import com.example.service.port.AccountStore;
import com.example.service.port.BankData.AccountData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.service.BankServiceException.Code.ACCOUNT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock AccountStore accounts;
    @Mock CurrentUserService currentUser;
    @InjectMocks AccountService service;

    @Test
    void getAccountsSortsAndMapsOnlyOwnedAccounts() {
        when(currentUser.requireUserId()).thenReturn("user-1");
        when(accounts.findByUserId("user-1")).thenReturn(List.of(
                account("savings", "user-1", AccountType.SAVINGS),
                account("checking", "user-1", AccountType.CHECKING)));

        var result = service.getAccounts();

        assertEquals(List.of("checking", "savings"), result.stream().map(a -> a.accountId()).toList());
        assertEquals(new BigDecimal("100.00"), result.getFirst().accountBalance());
    }

    @Test
    void missingOrForeignAccountHasSameNotFoundError() {
        when(currentUser.requireUserId()).thenReturn("user-1");
        // Strict Mockito rejects calls whose arguments weren't stubbed, so stub the "missing" lookup too
        when(accounts.findOwnedById("missing", "user-1")).thenReturn(Optional.empty());
        when(accounts.findOwnedById("foreign", "user-1"))
                .thenReturn(Optional.of(account("foreign", "user-2", AccountType.CHECKING)));

        BankServiceException missing = assertThrows(BankServiceException.class, () -> service.getAccount("missing"));
        BankServiceException foreign = assertThrows(BankServiceException.class, () -> service.getAccount("foreign"));

        assertEquals(ACCOUNT_NOT_FOUND, missing.getCode());
        assertEquals(missing.getMessage(), foreign.getMessage());
    }

    @Test
    void rejectsStoreReturningAnotherUsersAccount() {
        when(currentUser.requireUserId()).thenReturn("user-1");
        when(accounts.findByUserId("user-1"))
                .thenReturn(List.of(account("foreign", "user-2", AccountType.CHECKING)));

        assertThrows(IllegalStateException.class, () -> service.getAccounts());
    }

    private static AccountData account(String id, String owner, AccountType type) {
        return new AccountData(id, owner, id, type, new BigDecimal("100.00"), "USD");
    }
}
