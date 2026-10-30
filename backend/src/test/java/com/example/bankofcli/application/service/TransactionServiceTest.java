package com.example.bankofcli.application.service;

import com.example.bankofcli.application.service.AccountService;
import com.example.bankofcli.application.exception.BankServiceException;
import com.example.bankofcli.application.service.CurrentUserService;
import com.example.bankofcli.application.service.TransactionService;
import com.example.bankofcli.application.contract.BankContracts.AccountType;
import com.example.bankofcli.application.port.AccountStore;
import com.example.bankofcli.application.port.TransactionStore;
import com.example.bankofcli.application.port.BankData.AccountData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static com.example.bankofcli.application.exception.BankServiceException.Code.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock AccountStore accounts;
    @Mock TransactionStore transactions;
    @Mock AccountService accountService;
    @Mock CurrentUserService currentUser;
    @InjectMocks TransactionService service;

    @Test
    void depositUpdatesBalanceAndRecordsHistory() {
        when(currentUser.requireUserId()).thenReturn("user-1");
        when(accounts.lockOwnedById("account-1", "user-1"))
                .thenReturn(Optional.of(account("account-1", "100.00")));

        assertTrue(service.deposit("account-1", new BigDecimal("12.34")).success());

        verify(accounts).updateBalance("account-1", new BigDecimal("112.34"));
        verify(transactions).record(eq("account-1"), eq("DEPOSIT"), eq(new BigDecimal("12.34")),
                any(Instant.class), eq("Money deposit"));
    }

    @Test
    void insufficientFundsLeavesBalanceAndHistoryUntouched() {
        when(currentUser.requireUserId()).thenReturn("user-1");
        when(accounts.lockOwnedById("account-1", "user-1"))
                .thenReturn(Optional.of(account("account-1", "100.00")));

        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.withdraw("account-1", new BigDecimal("100.01")));

        assertEquals(INSUFFICIENT_FUNDS, error.getCode());
        verify(accounts, never()).updateBalance(anyString(), any(BigDecimal.class));
        verifyNoInteractions(transactions);
    }

    @Test
    void transferUpdatesBothBalancesAndRecordsBothSides() {
        when(currentUser.requireUserId()).thenReturn("user-1");
        when(accounts.lockOwnedById("checking", "user-1"))
                .thenReturn(Optional.of(account("checking", "100.00")));
        when(accounts.lockById("savings"))
                .thenReturn(Optional.of(account("savings", "50.00")));

        assertTrue(service.transfer("checking", "savings", new BigDecimal("25.00")).success());

        verify(accounts).updateBalance("checking", new BigDecimal("75.00"));
        verify(accounts).updateBalance("savings", new BigDecimal("75.00"));
        verify(accounts).lockById("savings");
        verify(transactions).record(eq("checking"), eq("TRANSFER_OUT"), eq(new BigDecimal("25.00")),
                any(Instant.class), eq("Transfer to savings"));
        verify(transactions).record(eq("savings"), eq("TRANSFER_IN"), eq(new BigDecimal("25.00")),
                any(Instant.class), eq("Transfer from checking"));
    }

    @Test
    void invalidAmountIsRejectedBeforeLockingAccount() {
        when(currentUser.requireUserId()).thenReturn("user-1");

        BankServiceException error = assertThrows(BankServiceException.class,
                () -> service.deposit("account-1", new BigDecimal("1.001")));

        assertEquals(INVALID_AMOUNT, error.getCode());
        verifyNoInteractions(accounts, transactions);
    }

    private static AccountData account(String id, String balance) {
        return new AccountData(id, "user-1", id, AccountType.CHECKING, new BigDecimal(balance), "USD");
    }
}
