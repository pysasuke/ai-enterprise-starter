package com.aistarter.auth.service;

import com.aistarter.auth.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties("test-secret-key-at-least-32-characters-long", 3600000));
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken("admin");
        assertTrue(jwtService.isTokenValid(token));
        assertEquals("admin", jwtService.extractUsername(token));
    }

    @Test
    void invalidTokenShouldBeRejected() {
        assertFalse(jwtService.isTokenValid("invalid.token.value"));
    }
}
