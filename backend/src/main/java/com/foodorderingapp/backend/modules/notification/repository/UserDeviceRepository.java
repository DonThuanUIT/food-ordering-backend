package com.foodorderingapp.backend.modules.notification.repository;

import com.foodorderingapp.backend.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {
    // Tìm xem token này đã có trong hệ thống chưa
    Optional<UserDevice> findByFcmToken(String fcmToken);

    // Tìm tất cả thiết bị của 1 user (để bắn thông báo đến tất cả máy họ đang dùng)
    Iterable<UserDevice> findAllByUserId(UUID userId);
}