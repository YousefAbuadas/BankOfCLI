package com.example.service;

import com.example.service.contract.BankContracts.*;
import com.example.service.port.BankData.*;

final class ServiceMapper {
    private ServiceMapper() {}

    static UserIdentity identity(UserData user) {
        return new UserIdentity(user.userId(), user.firstName(), user.lastName(), user.email(), user.role());
    }

    static AccountResponse account(AccountData account) {
        return new AccountResponse(account.accountId(), new UserReference(account.userId()), account.currency(),
                account.balance(), account.type(), account.name());
    }

    static TransactionResponse transaction(TransactionData entry) {
        return new TransactionResponse(entry.transactionId(), new AccountReference(entry.accountId()),
                entry.type(), entry.amount(), entry.date().toString(), entry.description());
    }
}
