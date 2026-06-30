package com.aistarter.auth.service;

import com.aistarter.auth.dto.LoginRequest;
import com.aistarter.auth.entity.User;
import com.aistarter.auth.repository.UserRepository;
import com.aistarter.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    @Test
    void loginSuccessShouldReturnToken() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encoded");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("admin123", "encoded")).thenReturn(true);
        when(jwtService.generateToken("admin")).thenReturn("token-123");

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");
        var response = authService.login(request);
        assertEquals("token-123", response.getToken());
        assertEquals("admin", response.getUsername());
    }

    @Test
    void loginWithWrongPasswordShouldFail() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("encoded");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");
        assertThrows(BusinessException.class, () -> authService.login(request));
    }
}
