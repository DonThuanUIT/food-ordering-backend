package com.foodorderingapp.backend.modules.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequest {
    @NotBlank(message = "FCM Token is required")
    private String fcmToken;
    private String deviceInfo; // Ví dụ: "Android 14 - Samsung S24"
}