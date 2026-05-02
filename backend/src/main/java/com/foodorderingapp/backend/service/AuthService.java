package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.LoginRequest;
import com.foodorderingapp.backend.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.dto.request.VerifyOtpRequest;
import com.foodorderingapp.backend.dto.response.AuthResponse;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.entity.enums.ShopStatus;
import com.foodorderingapp.backend.entity.enums.UserRole;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.ShopRepository;
import com.foodorderingapp.backend.repository.UserRepository;
import com.foodorderingapp.backend.security.JwtUtil;
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
    private final JwtUtil jwtUtil;

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
        shop.setStatus(ShopStatus.PENDING);
        shop.setIsActive(false);
        shopRepository.save(shop);

        String otpCode = otpService.generateAndSaveOtp(request.getEmail());

        emailService.sendOtpEmail(request.getEmail(), otpCode);

        return buildAuthResponse(user, "Restaurant Owner registration successful! The system is waiting for Admin to approve your shop.");
    }

    private AuthResponse generateAuthResponse(User user, String message) {
        String accessToken = jwtUtil.generateAccessToken(user.getPhone(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getPhone());

        return AuthResponse.builder()
                .message(message)
                .phone(user.getPhone())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AppException("Incorrect account or password!", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Incorrect account or password!", HttpStatus.UNAUTHORIZED);
        }

        if (!user.getIsEmailVerified()) {
            throw new AppException("The account has not been email verified. Please check your mailbox!", HttpStatus.FORBIDDEN);
        }

        log.info("User {} has logged in successfully", user.getPhone());

        return generateAuthResponse(user, "Login successfully!");
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        boolean isValidOtp = otpService.validateOtp(request.getEmail(), request.getOtpCode());
        if (!isValidOtp) {
            throw new AppException("Invalid or expired OTP code!", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("No account found with this email!", HttpStatus.NOT_FOUND));

        if (user.getIsEmailVerified()) {
            throw new AppException("This account has already been verified!", HttpStatus.BAD_REQUEST);
        }
        user.setIsEmailVerified(true);
        userRepository.save(user);

        log.info("User {} has verified his email and logged in successfully", user.getPhone());

        return generateAuthResponse(user, "Verification successful! Welcome to the system.");
    }


    private void validateNewUser(String phone, String email) {
        if(userRepository.existsByPhone(phone)) {
            throw new AppException("This phone number is registered!", HttpStatus.BAD_REQUEST);
        }
        if(userRepository.existsByEmail(email)) {
            throw new AppException("This email has been used!", HttpStatus.BAD_REQUEST);
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
                .build();
    }
}