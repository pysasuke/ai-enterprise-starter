package com.aistarter.auth.service;

import com.aistarter.auth.dto.LoginRequest;
import com.aistarter.auth.dto.LoginResponse;
import com.aistarter.auth.entity.User;
import com.aistarter.auth.repository.UserRepository;
import com.aistarter.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtService.generateToken(user.getUsername());
        return new LoginResponse(token, user.getUsername());
    }
}
