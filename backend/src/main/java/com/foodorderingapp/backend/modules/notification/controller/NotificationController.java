package com.foodorderingapp.backend.modules.notification.controller;

import com.foodorderingapp.backend.modules.notification.dto.request.FcmTokenRequest;
import com.foodorderingapp.backend.modules.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/device-token")
    public ResponseEntity<Void> registerDeviceToken(@Valid @RequestBody FcmTokenRequest request) {
        notificationService.registerDeviceToken(request.getFcmToken(), request.getDeviceInfo());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/device-token")
    public ResponseEntity<Void> removeDeviceToken(@RequestParam("fcmToken") String fcmToken) {
        notificationService.removeDeviceToken(fcmToken);
        return ResponseEntity.ok().build();
    }
}