package com.example.BankofCLI.JWTTests;

import com.example.BankofCLI.models.User;
import com.example.JWT.JwtUtil;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilTest {
    private static final String SECRET = Encoders.BASE64.encode(
            "a-test-signing-key-long-enough-for-hs256".getBytes(StandardCharsets.UTF_8));

    @Test
    void generatedTokenContainsIdentityAndExpiration() {
        JwtUtil jwt = new JwtUtil(SECRET, 60_000);
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("ada@example.com");
        when(user.getId()).thenReturn("user-1");

        var claims = jwt.parseClaims(jwt.generateToken(user));

        assertEquals("ada@example.com", claims.getSubject());
        assertEquals("user-1", claims.get("Id", String.class));
        assertEquals("bankofcli", claims.getIssuer());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtUtil issuer = new JwtUtil(SECRET, 60_000);
        JwtUtil verifier = new JwtUtil(Encoders.BASE64.encode(
                "another-test-signing-key-for-hs256-use".getBytes(StandardCharsets.UTF_8)), 60_000);
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("ada@example.com");

        assertThrows(JwtException.class, () -> verifier.parseClaims(issuer.generateToken(user)));
    }
}
