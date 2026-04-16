package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.RegisterRequest;
import com.foodorderingapp.backend.dto.response.AuthResponse;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.entity.enums.RoleEnum;
import com.foodorderingapp.backend.entity.enums.UserStatusEnum;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthResponse register(RegisterRequest request) {
        log.info("Phone number registration is being processed: {}", request.getPhone());
        if(userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.warn("Registration failed: Phone {} already exists", request.getPhone());
            throw new AppException("This phone number has already been registered in the system!", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setPhone (request.getPhone());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));


        user.setRole(RoleEnum.STUDENT);
        user.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful!")
                .phone(user.getPhone())
                .build();
    }

        }




