package com.carenest.backend.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generateToken_acceptsRawUtf8SecretFromEnvironment() {
        JwtService jwtService = new JwtService();
        User userDetails = new User(
                "patient@example.com",
                "password",
                java.util.List.of()
        );

        ReflectionTestUtils.setField(jwtService, "secretKey", "change-me-long-random-jwt-secret-at-least-32-bytes");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        String token = jwtService.generateToken(userDetails);

        assertEquals("patient@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
