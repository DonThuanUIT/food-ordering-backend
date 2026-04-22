package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.dto.response.AuthResponse;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.entity.enums.ShopStatus;
import com.foodorderingapp.backend.entity.enums.UserRole;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.ShopRepository;
import com.foodorderingapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    private final OtpService otpService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse registerStudent(StudentRegisterRequest request) {
        validateNewUser(request.getPhone(), request.getEmail());

        User user = createBaseUser(request.getPhone(), request.getEmail(), request.getFullName(), request.getPassword());
        user.setRole(UserRole.STUDENT);

        // Logic riêng của Student
        // user.setBuildingId(request.getBuildingId()); // Mở comment nếu bạn đã setup mapping Building

        userRepository.save(user);

        String otpCode = otpService.generateAndSaveOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otpCode);

        return buildAuthResponse(user, "Successfully registered for a Student account! Please check your email for confirmation.");
    }

    @Transactional
    public AuthResponse registerVendor(VendorRegisterRequest request) {
        validateNewUser(request.getPhone(), request.getEmail());

        User user = createBaseUser(request.getPhone(), request.getEmail(), request.getFullName(), request.getPassword());
        user.setRole(UserRole.VENDOR);
        userRepository.save(user);

        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(request.getShopName());
        shop.setDescription(request.getDescription());
        shop.setOpenTime(request.getOpenTime());
        shop.setCloseTime(request.getCloseTime());
        shop.setStatus(ShopStatus.PENDING); // Đợi Admin duyệt
        shop.setIsActive(false);
        shopRepository.save(shop);

        String otpCode = otpService.generateAndSaveOtp(request.getEmail());

        emailService.sendOtpEmail(request.getEmail(), otpCode);

        return buildAuthResponse(user, "Restaurant Owner registration successful! The system is waiting for Admin to approve your shop.");
    }


    private void validateNewUser(String phone, String email) {
        if(userRepository.existsByPhone(phone)) {
            throw new AppException("This phone number is registered!", HttpStatus.BAD_REQUEST);
        }
        if(userRepository.existsByEmail(email)) {
            throw new AppException("Email này đã được sử dụng!", HttpStatus.BAD_REQUEST);
        }
    }

    private User createBaseUser(String phone, String email, String fullName, String rawPassword) {
        User user = new User();
        user.setPhone(phone);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setIsEmailVerified(false);
        return user;
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        return AuthResponse.builder()
                .message(message)
                .phone(user.getPhone())
                // Ở đây bạn có thể trả về null cho token nếu bắt họ phải xác thực email mới cho login
                .build();
    }
}