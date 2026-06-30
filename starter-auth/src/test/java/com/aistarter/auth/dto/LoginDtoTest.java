package com.aistarter.auth.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginDtoTest {

    @Test
    void loginRequestShouldHoldCredentials() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        assertEquals("admin", request.getUsername());
        assertEquals("admin123", request.getPassword());
    }

    @Test
    void loginResponseShouldHoldToken() {
        LoginResponse response = new LoginResponse("token", "admin");
        assertEquals("token", response.getToken());
        assertEquals("admin", response.getUsername());
    }
}
