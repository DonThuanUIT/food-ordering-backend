package com.foodorderingapp.backend.modules.notification.service;

import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.entity.UserDevice;
import com.foodorderingapp.backend.modules.notification.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerDeviceToken(String fcmToken, String deviceInfo) {
        // 1. Lấy thông tin User đang đăng nhập từ Security Context (Token)
        String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Kiểm tra xem Token này đã tồn tại chưa (tránh insert rác database)
        Optional<UserDevice> existingDevice = userDeviceRepository.findByFcmToken(fcmToken);

        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();
            // Nếu token này chuyển sang tài khoản khác đăng nhập trên cùng 1 máy -> Cập nhật lại chủ sở hữu
            if (!device.getUser().getId().equals(currentUser.getId())) {
                device.setUser(currentUser);
            }
            device.setLastActiveAt(LocalDateTime.now());
            userDeviceRepository.save(device);
            log.info("Updated existing FCM Token for user: {}", currentUser.getPhone());
        } else {
            // Khởi tạo thiết bị mới
            UserDevice newDevice = UserDevice.builder()
                    .user(currentUser)
                    .fcmToken(fcmToken)
                    .deviceInfo(deviceInfo)
                    .lastActiveAt(LocalDateTime.now())
                    .build();
            userDeviceRepository.save(newDevice);
            log.info("Registered new FCM Token for user: {}", currentUser.getPhone());
        }
    }

    @Transactional
    public void removeDeviceToken(String fcmToken) {
        // Tìm thiết bị chứa token này
        Optional<UserDevice> existingDevice = userDeviceRepository.findByFcmToken(fcmToken);

        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();
            // Xác minh bảo mật: Chỉ cho phép xóa nếu người đang gọi API đúng là chủ của thiết bị
            String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userRepository.findByPhone(userPhone)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (device.getUser().getId().equals(currentUser.getId())) {
                userDeviceRepository.delete(device);
                log.info("Revoked FCM Token successfully for user: {}", currentUser.getPhone());
            } else {
                log.warn("Security Alert: User {} tried to delete FCM token of another user", currentUser.getPhone());
            }
        }
    }
}