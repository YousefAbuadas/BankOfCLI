package com.example.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

//This Class is responsible for filtering incoming requests and validating JWTs

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // Runs once per request — checks for a JWT in the Authorization header
    // If valid, sets the authenticated user in Spring Security's context
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // JWTs are categorized as something called "bearer tokens"
        if (authHeader != null && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.parseClaims(token); // parse + verify once

                // AuthUser is (userId, email): the id comes from the "Id" claim, the email is the subject
                AuthUser user = new AuthUser(claims.get("Id", String.class), claims.getSubject());
                String role = claims.get("role", String.class);

                // Spring's class is called "UsernamePassword..." but the principal can be anything
                // Credentials are null - we never carry the password around after login
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + (role == null ? "USER" : role)))
                        );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);

            } catch (JwtException | IllegalArgumentException e) {
                // Tampered / expired / malformed token -> request stays unauthenticated
                // Protected endpoints will then answer 401 (see SecurityConfig)
                SecurityContextHolder.clearContext();
            }
        }

        // Always continue the filter chain regardless of auth outcome
        filterChain.doFilter(request, response);
    }
}
