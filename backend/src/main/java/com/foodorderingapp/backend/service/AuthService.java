package com.foodorderingapp.backend.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void testBCrypt() {
        String rawPassword = "Test@123";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        log.info("========== TEST BCRYPT INJECTION ==========");
        log.info("Mật khẩu người dùng nhập: {}", rawPassword);
        log.info("Mật khẩu băm (Sẽ lưu xuống Database): {}", encodedPassword);

        boolean isMatch = passwordEncoder.matches("Test@123", encodedPassword);
        log.info("Mật khẩu nhập lại có khớp không? {}", isMatch);
        log.info("===========================================");

    }
}


