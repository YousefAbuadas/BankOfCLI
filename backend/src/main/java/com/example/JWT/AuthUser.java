package com.example.JWT;

// The logged-in user, built from a valid JWT by JwtFilter
// Controllers can get it with:  @AuthenticationPrincipal AuthUser me
public record AuthUser(String userId, String email) {
}
