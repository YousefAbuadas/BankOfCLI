package com.example.JWT;

import com.example.service.port.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Tells the service layer who is logged in, using the AuthUser that JwtFilter
// put into Spring Security's context from a verified token.
// Never reads a user id from the URL or request body.
@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<String> userId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUser me) {
            return Optional.ofNullable(me.userId());
        }
        return Optional.empty();
    }
}
