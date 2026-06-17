package com.foodorderingapp.backend.modules.auth;

import com.foodorderingapp.backend.modules.auth.dto.request.LoginRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.ResendOtpRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.VerifyOtpRequest;
import com.foodorderingapp.backend.modules.auth.dto.response.AuthResponse;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.core.enums.UserRole;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.core.security.JwtUtil;
import com.foodorderingapp.backend.modules.auth.AuthService;
import com.foodorderingapp.backend.modules.email.EmailService;
import com.foodorderingapp.backend.modules.email.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public AuthResponse registerStudent(StudentRegisterRequest request) {
        User user = prepareUserForRegistration(request.getPhone(), request.getEmail(), request.getFullName(), request.getPassword());
        user.setRole(UserRole.STUDENT);

        userRepository.save(user);

        String otpCode = otpService.generateAndSaveOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otpCode);

        return buildAuthResponse(user, "Đăng ký tài khoản Sinh viên thành công! Vui lòng kiểm tra email để xác thực.");
    }

    @Override
    @Transactional
    public AuthResponse registerVendor(VendorRegisterRequest request) {
        // 1. Gọi hàm tiền xử lý
        User user = prepareUserForRegistration(request.getPhone(), request.getEmail(), request.getFullName(), request.getPassword());
        user.setRole(UserRole.VENDOR);
        userRepository.save(user);

        // 2. [QUAN TRỌNG] Dọn dẹp quán cũ nếu đây là tài khoản chưa verify đăng ký lại
        if (user.getId() != null) {
            List<Shop> oldShops = shopRepository.findAllByOwnerId(user.getId());
            if (!oldShops.isEmpty()) {
                shopRepository.deleteAll(oldShops);
            }
        }

        // 3. Tạo quán mới
        Shop shop = new Shop();
        shop.setOwner(user);
        shop.setName(request.getShopName());
        shop.setDescription(request.getDescription());
        shop.setOpenTime(request.getOpenTime());
        shop.setCloseTime(request.getCloseTime());
        shop.setStatus(ShopStatus.PENDING);
        shop.setIsActive(false);
        shopRepository.save(shop);

        // 4. Gửi OTP
        String otpCode = otpService.generateAndSaveOtp(request.getEmail());
        emailService.sendOtpEmail(request.getEmail(), otpCode);

        return buildAuthResponse(user, "Đăng ký Chủ quán thành công! Hệ thống đang chờ xác thực email và Admin duyệt.");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new AppException("Tài khoản hoặc mật khẩu không chính xác!", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Tài khoản hoặc mật khẩu không chính xác!", HttpStatus.UNAUTHORIZED);
        }

        if (!user.getIsEmailVerified()) {
            throw new AppException("Tài khoản chưa được xác thực email. Vui lòng kiểm tra hòm thư!", HttpStatus.FORBIDDEN);
        }

        log.info("User {} has logged in successfully", user.getPhone());
        return generateAuthResponse(user, "Đăng nhập thành công!");
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        boolean isValidOtp = otpService.validateOtp(request.getEmail(), request.getOtpCode());
        if (!isValidOtp) {
            throw new AppException("Mã OTP không hợp lệ hoặc đã hết hạn!", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản với email này!", HttpStatus.NOT_FOUND));

        if (user.getIsEmailVerified()) {
            throw new AppException("Tài khoản này đã được xác thực từ trước!", HttpStatus.BAD_REQUEST);
        }
        user.setIsEmailVerified(true);
        userRepository.save(user);

        log.info("User {} has verified his email and logged in successfully", user.getPhone());
        return generateAuthResponse(user, "Xác thực thành công! Chào mừng bạn đến với hệ thống.");
    }

    // ================== CÁC HÀM PRIVATE HỖ TRỢ NGHIỆP VỤ ==================

    private User prepareUserForRegistration(String phone, String email, String fullName, String rawPassword) {
        // Kiểm tra theo Phone
        Optional<User> userByPhone = userRepository.findByPhone(phone);
        if (userByPhone.isPresent()) {
            User existingUser = userByPhone.get();
            if (existingUser.getIsEmailVerified()) {
                throw new AppException("Số điện thoại này đã được đăng ký và xác thực!", HttpStatus.BAD_REQUEST);
            }
            return updateUnverifiedUser(existingUser, phone, email, fullName, rawPassword);
        }

        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User existingUser = userByEmail.get();
            if (existingUser.getIsEmailVerified()) {
                throw new AppException("Email này đã được sử dụng và xác thực!", HttpStatus.BAD_REQUEST);
            }
            return updateUnverifiedUser(existingUser, phone, email, fullName, rawPassword);
        }

        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setIsEmailVerified(false);
        return newUser;
    }

    private User updateUnverifiedUser(User user, String phone, String email, String fullName, String rawPassword) {
        user.setPhone(phone);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return user;
    }

    private AuthResponse generateAuthResponse(User user, String message) {
        String accessToken = jwtUtil.generateAccessToken(user.getPhone(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getPhone());

        return AuthResponse.builder()
                .message(message)
                .phone(user.getPhone())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        return AuthResponse.builder()
                .message(message)
                .phone(user.getPhone())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Không tìm thấy tài khoản với email này!", HttpStatus.NOT_FOUND));

        if (user.getIsEmailVerified()) {
            throw new AppException("Tài khoản này đã được xác thực từ trước. Vui lòng đăng nhập!", HttpStatus.BAD_REQUEST);
        }

        String newOtpCode = otpService.generateAndSaveOtp(user.getEmail());

        emailService.sendOtpEmail(user.getEmail(), newOtpCode);

        log.info("Đã gửi lại mã OTP cho user: {}", user.getEmail());

        return buildAuthResponse(user, "Mã OTP mới đã được gửi. Vui lòng kiểm tra email của bạn!");
    }
}