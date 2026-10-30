package com.example.bankofcli.infrastructure.security;

// The logged-in user, built from a valid JWT by JwtFilter
// Controllers can get it with:  @AuthenticationPrincipal AuthUser me
public record AuthUser(String userId, String email) {
}
