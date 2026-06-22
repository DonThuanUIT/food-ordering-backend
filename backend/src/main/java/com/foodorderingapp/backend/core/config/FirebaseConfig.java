package com.foodorderingapp.backend.core.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Kiểm tra xem Firebase đã được khởi tạo chưa để tránh lỗi khởi tạo nhiều lần
            if (FirebaseApp.getApps().isEmpty()) {
                // Đọc file json từ thư mục resources
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info(" Firebase Admin SDK initialized successfully!");
            }
        } catch (IOException e) {
            log.error(" Failed to initialize Firebase Admin SDK", e);
        }
    }
}