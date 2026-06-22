package com.foodorderingapp.backend.modules.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FcmService {

    public void sendPushNotification(String targetToken, String title, String body, Map<String, String> dataPayload) {
        try {
            // 1. Đóng gói giao diện hiển thị (Phần chữ hiện lên màn hình khóa điện thoại)
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 2. Đóng gói toàn bộ tin nhắn (Bao gồm UI và Dữ liệu ngầm để Android điều hướng)
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification)
                    .putAllData(dataPayload) // Gửi kèm type và referenceId ở đây
                    .build();

            // 3. Bóp cò bắn lên trạm Firebase
            String response = FirebaseMessaging.getInstance().send(message);
            log.info(" Bắn thông báo thành công tới token: {}. Phản hồi từ Firebase: {}", targetToken, response);

        } catch (Exception e) {
            log.error(" Gửi thông báo thất bại tới token: {}", targetToken, e);
            // Kịch bản nâng cao: Nếu lỗi do token hết hạn (UNREGISTERED), ta có thể xóa token khỏi DB.
            // Tạm thời ở MVP ta cứ log ra để theo dõi.
        }
    }
}