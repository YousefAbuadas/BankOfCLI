package com.example.service;

import com.example.service.contract.BankContracts.*;
import com.example.service.port.*;
import com.example.service.port.BankData.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import static com.example.service.BankServiceException.Code.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class TransactionService {
    private final AccountStore accounts;
    private final TransactionStore transactions;
    private final AccountService accountService;
    private final CurrentUserService currentUser;

    public TransactionService(AccountStore accounts, TransactionStore transactions,
                              AccountService accountService, CurrentUserService currentUser) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.accountService = accountService;
        this.currentUser = currentUser;
    }

    public OperationResponse deposit(String accountId, BigDecimal dollarAmount) {
        String userId = currentUser.requireUserId();
        BigDecimal amount = Money.amount(dollarAmount);
        AccountData account = lock(accountId, userId);
        Money.requireUsd(account.currency());
        Money.checkCredit(account.balance(), amount);
        accounts.updateBalance(accountId, account.balance().add(amount));
        transactions.record(accountId, "DEPOSIT", amount, Instant.now(), "Money deposit");
        return new OperationResponse(true);
    }

    public OperationResponse withdraw(String accountId, BigDecimal dollarAmount) {
        String userId = currentUser.requireUserId();
        BigDecimal amount = Money.amount(dollarAmount);
        AccountData account = lock(accountId, userId);
        Money.requireUsd(account.currency());
        requireFunds(account, amount);
        accounts.updateBalance(accountId, account.balance().subtract(amount));
        transactions.record(accountId, "WITHDRAWAL", amount, Instant.now(), "Cash withdrawal");
        return new OperationResponse(true);
    }

    /** Transfers between the customer's accounts or to another account in the bank. */
    public OperationResponse transfer(String sourceId, String destinationId, BigDecimal dollarAmount) {
        String userId = currentUser.requireUserId();
        BigDecimal amount = Money.amount(dollarAmount);
        AccountService.requireAccountId(sourceId);
        if (destinationId == null || destinationId.isBlank() || sourceId.equals(destinationId)) {
            throw new BankServiceException(INVALID_DESTINATION, "Choose a different account to receive the transfer.");
        }

        // Consistent lock order prevents opposite-direction transfers from taking opposing locks.
        AccountData source;
        AccountData destination;
        if (sourceId.compareTo(destinationId) < 0) {
            source = lock(sourceId, userId);
            destination = lockDestination(destinationId);
        } else {
            destination = lockDestination(destinationId);
            source = lock(sourceId, userId);
        }

        Money.requireUsd(source.currency());
        Money.requireUsd(destination.currency());
        requireFunds(source, amount);
        Money.checkCredit(destination.balance(), amount);
        Instant date = Instant.now();

        accounts.updateBalance(sourceId, source.balance().subtract(amount));
        accounts.updateBalance(destinationId, destination.balance().add(amount));
        transactions.record(sourceId, "TRANSFER_OUT", amount, date, "Transfer to " + destination.name());
        transactions.record(destinationId, "TRANSFER_IN", amount, date, "Transfer from " + source.name());
        // Do not catch persistence failures: Spring must roll back both balances and both entries.
        return new OperationResponse(true);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(String accountId) {
        accountService.requireOwnedAccount(accountId);
        List<TransactionData> entries = transactions.findByAccountId(accountId);
        if (entries.stream().anyMatch(entry -> !accountId.equals(entry.accountId()))) {
            throw new IllegalStateException("TransactionStore returned another account's history.");
        }
        return entries.stream()
                .sorted(Comparator.comparing(TransactionData::date)
                        .thenComparing(TransactionData::transactionId).reversed())
                .map(ServiceMapper::transaction).toList();
    }

    private AccountData lock(String accountId, String userId) {
        AccountService.requireAccountId(accountId);
        AccountData account = accounts.lockOwnedById(accountId, userId).orElseThrow(AccountService::notFound);
        AccountService.verifyOwnership(account, accountId, userId);
        return account;
    }

    private AccountData lockDestination(String accountId) {
        AccountData account = accounts.lockById(accountId).orElseThrow(AccountService::notFound);
        if (!accountId.equals(account.accountId())) throw AccountService.notFound();
        return account;
    }

    private static void requireFunds(AccountData account, BigDecimal amount) {
        if (account.balance().compareTo(amount) < 0) {
            throw new BankServiceException(INSUFFICIENT_FUNDS, "This amount is higher than your available balance.");
        }
    }
}
