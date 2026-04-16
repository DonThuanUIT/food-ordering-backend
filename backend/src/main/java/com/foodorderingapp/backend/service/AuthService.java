package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.RegisterRequest;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.entity.enums.RoleEnum;
import com.foodorderingapp.backend.entity.enums.UserStatusEnum;
import com.foodorderingapp.backend.repository.UserRepository;
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
    private final UserRepository userRepository;

    public String register(RegisterRequest request) {
        log.info("Đang xử lý đăng ký cho số điện thoại: {}", request.getPhone());
        if(userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.warn("Dang ky that bai, sdt da ton tai", request.getPhone());
            throw new RuntimeException("So dien thoai da dc dki");
        }

        User user = new User();
        user.setPhone (request.getPhone());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));


        user.setRole(RoleEnum.STUDENT);
        user.setStatus(UserStatusEnum.ACTIVE);
        userRepository.save(user);

        log.info("Người dùng {} đã đăng ký thành công", request.getPhone());
        return "Đăng ký thành công!";
    }

        }




