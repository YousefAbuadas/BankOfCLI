package com.example.bankofcli.application.port;

import com.example.bankofcli.application.contract.BankContracts.UserIdentity;

@FunctionalInterface
public interface TokenIssuer {
    /**
     * Delegate to the team's JWT utility. Include subject=email and userId/role claims,
     * set expiration, and sign with the configured persistent key. No password is supplied.
     */
    String generateToken(UserIdentity user);
}
