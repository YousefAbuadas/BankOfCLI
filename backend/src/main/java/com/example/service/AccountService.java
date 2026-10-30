package com.example.service;

import com.example.service.contract.BankContracts.AccountResponse;
import com.example.service.port.AccountStore;
import com.example.service.port.BankData.AccountData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import static com.example.service.BankServiceException.Code.ACCOUNT_NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private final AccountStore accounts;
    private final CurrentUserService currentUser;

    public AccountService(AccountStore accounts, CurrentUserService currentUser) {
        this.accounts = accounts;
        this.currentUser = currentUser;
    }

    public List<AccountResponse> getAccounts() {
        String userId = currentUser.requireUserId();
        List<AccountData> owned = accounts.findByUserId(userId);
        if (owned.stream().anyMatch(account -> !userId.equals(account.userId()))) {
            throw new IllegalStateException("AccountStore returned an account belonging to another user.");
        }
        return owned.stream()
                .sorted(Comparator.comparing(AccountData::type).thenComparing(AccountData::accountId))
                .map(ServiceMapper::account).toList();
    }

    public AccountResponse getAccount(String accountId) {
        return ServiceMapper.account(requireOwnedAccount(accountId));
    }

    public BigDecimal getBalance(String accountId) {
        return getAccount(accountId).accountBalance();
    }

    AccountData requireOwnedAccount(String accountId) {
        String userId = currentUser.requireUserId();
        requireAccountId(accountId);
        AccountData account = accounts.findOwnedById(accountId, userId).orElseThrow(AccountService::notFound);
        verifyOwnership(account, accountId, userId);
        return account;
    }

    static void requireAccountId(String accountId) {
        if (accountId == null || accountId.isBlank()) throw notFound();
    }

    static void verifyOwnership(AccountData account, String accountId, String userId) {
        if (!userId.equals(account.userId()) || !accountId.equals(account.accountId())) throw notFound();
    }

    static BankServiceException notFound() {
        // Do not reveal whether another customer's account exists.
        return new BankServiceException(ACCOUNT_NOT_FOUND, "Please choose one of your accounts.");
    }
}
