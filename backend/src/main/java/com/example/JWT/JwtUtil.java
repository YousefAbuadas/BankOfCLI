package com.example.JWT;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.BankofCLI.models.User;
import com.example.service.contract.BankContracts.UserIdentity;
import com.example.service.port.TokenIssuer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

//This Class is responsible for generating and reading JWTs

@Component
public class JwtUtil implements TokenIssuer {

    private static final String ISSUER = "bankofcli";

    // The signing key comes from application.properties (jwt.secret), so tokens
    // survive restarts and every running copy of the app uses the same key
    private final Key key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
    }

    // Generates a signed JWT: subject = email, plus role and userId as custom claims
    // NEVER put a password in a token - the payload is only base64, anyone can read it
    public String generateToken(User user) {
        return buildToken(user.getEmail(), user.getId(), user.getRole());
    }

    // Called by the service layer's AuthService (TokenIssuer port) after login/register
    @Override
    public String generateToken(UserIdentity user) {
        return buildToken(user.email(), user.userId(), user.role());
    }

    private String buildToken(String email, String userId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(email)                    //who the user is
                .claim("Id", userId)                  //claims are just other pieces of info
                .claim("role", role == null ? "USER" : role)
                .setIssuer(ISSUER)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256) //the "signature", which proves we made it
                .compact();
    }

    // Verifies signature, expiration and issuer in ONE pass, then returns the claims
    // Throws a JwtException if the token was tampered with, is expired, or is malformed
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(ISSUER)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
