package com.example.bankofcli.infrastructure.security;

import com.example.bankofcli.infrastructure.security.jwt.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

//This class configures Spring Security for the application
//Stuff like who can access what methods, and what kinds of requests are allowed

@Configuration //Class-based bean configuration! SprSec encourages this
@EnableMethodSecurity // lets you use @PreAuthorize("hasRole('ADMIN')") on methods
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // Defines which endpoints are public and which require authentication
    // Also registers our JwtFilter to run before Spring's default auth filter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults()) // uses the corsConfigurationSource bean below
                .csrf(csrf -> csrf.disable())    // fine for bearer tokens (no auth cookies)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex
                        // no/invalid/expired token -> 401 (tells the frontend to send the user to login)
                        .authenticationEntryPoint((request, response, e) ->
                                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid token"))
                        // valid token but wrong role -> 403
                        .accessDeniedHandler((request, response, e) ->
                                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Access denied"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // allow preflight
                        .requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/auth/**").permitAll()  // login and register are public
                        .requestMatchers(HttpMethod.GET, "/user/getAccount", "/user/accounts").authenticated()
                        .requestMatchers(HttpMethod.PUT,
                                "/transaction/deposit/*", "/transaction/withdraw/*", "/transaction/transfer/*")
                                .authenticated()
                        .requestMatchers(HttpMethod.GET, "/transaction/transactions/*").authenticated()
                        .requestMatchers("/error").permitAll()    // so real errors aren't hidden behind a 401
                        // Keep unknown paths public to Spring MVC so they retain their normal 404/405 responses.
                        // Keep banking matchers in sync with controllers; services also require the current user.
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    // CORS is centralized here; production supplies the exact CloudFront origin.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.allowed-origin}") String allowedOrigin) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Instead of storing password as plaintext and comparing, use BCrypt to hash password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
