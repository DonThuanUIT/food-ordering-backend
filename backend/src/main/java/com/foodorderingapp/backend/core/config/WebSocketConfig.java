package com.foodorderingapp.backend.core.config;

import com.foodorderingapp.backend.core.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Mobile App sẽ gọi vào địa chỉ: ws://10.0.2.2:8080/api/ws-chat
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*"); // MVP: Cho phép mọi thiết bị kết nối
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Các tin nhắn gửi từ Mobile LÊN Server sẽ bắt đầu bằng /app
        registry.setApplicationDestinationPrefixes("/app");

        // Các tin nhắn Server đẩy XUỐNG Mobile sẽ bắt đầu bằng /topic hoặc /user
        registry.enableSimpleBroker("/topic", "/queue", "/user");

        // Tiền tố riêng cho tính năng chat 1-1
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Gắn "Trạm gác" đã tạo ở Bước 5.2 vào cửa ngõ hệ thống
        registration.interceptors(authInterceptor);
    }
}