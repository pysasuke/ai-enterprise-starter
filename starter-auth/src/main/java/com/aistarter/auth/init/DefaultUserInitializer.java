package com.aistarter.auth.init;

import com.aistarter.auth.entity.User;
import com.aistarter.auth.repository.UserRepository;
import com.aistarter.common.constant.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(AppConstants.DEFAULT_USER)) {
            User user = new User();
            user.setUsername(AppConstants.DEFAULT_USER);
            user.setPassword(passwordEncoder.encode(AppConstants.DEFAULT_PASSWORD));
            user.setRole("ADMIN");
            userRepository.save(user);
            log.info("Default admin user created: {}", AppConstants.DEFAULT_USER);
        }
    }
}
