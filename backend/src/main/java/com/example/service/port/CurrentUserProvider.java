package com.example.service.port;

import java.util.Optional;

@FunctionalInterface
public interface CurrentUserProvider {
    /**
     * Return the ID from the authenticated principal established by the JWT security filter.
     * Return empty for missing, invalid or expired authentication.
     * Never obtain this value from an unverified token, query parameter or request body.
     */
    Optional<String> userId();
}
