package com.example.bankofcli.infrastructure.security.jwt;

import com.example.bankofcli.infrastructure.persistence.entity.User;
import com.example.bankofcli.infrastructure.security.jwt.JwtFilter;
import com.example.bankofcli.infrastructure.security.jwt.JwtUtil;
import io.jsonwebtoken.io.Encoders;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest {
    private final JwtUtil jwt = new JwtUtil(Encoders.BASE64.encode(
            "a-test-signing-key-long-enough-for-hs256".getBytes(StandardCharsets.UTF_8)), 60_000);
    private final JwtFilter filter = new JwtFilter(jwt);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenSetsAuthenticatedPrincipal() throws Exception {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("ada@example.com");
        when(user.getId()).thenReturn("user-1");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt.generateToken(user));
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        verify(chain).doFilter(any(), any());
    }

    @Test
    void malformedBearerTokenContinuesWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(chain).doFilter(any(), any());
    }
}
