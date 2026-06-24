package com.foodorderingapp.backend.core.websocket;

import com.foodorderingapp.backend.core.security.CustomUserDetailsService;
import com.foodorderingapp.backend.core.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    // Inject chính xác 2 class đang được dùng bên JwtAuthenticationFilter
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ kiểm tra Token ở lần "Bắt tay" (Handshake) đầu tiên
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // 1. Trích xuất số điện thoại (username) từ JWT
                    String userPhone = jwtUtil.extractUsername(token);

                    if (userPhone != null) {
                        // 2. Load thông tin User từ Database
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userPhone);

                        // 3. Kiểm tra bảo mật: Tài khoản có bị khóa không?
                        if (!userDetails.isAccountNonLocked()) {
                            log.warn("WebSocket blocked for locked user: {}", userPhone);
                            throw new IllegalArgumentException("Tài khoản đã bị khóa!");
                        }

                        // 4. Xác minh Token có hợp lệ không
                        if (jwtUtil.isTokenValid(token, userDetails)) {
                            // 5. Gán quyền vào phiên WebSocket
                            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                            accessor.setUser(auth); // Quan trọng: Đánh dấu kết nối này là của ai

                            log.info(" WebSocket Authenticated for user: {}", userPhone);
                        } else {
                            throw new IllegalArgumentException("Token hết hạn hoặc không hợp lệ");
                        }
                    }
                } catch (Exception e) {
                    log.error(" WebSocket Authentication failed: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid JWT Token"); // Cắt đứt kết nối
                }
            } else {
                log.warn(" Missing Authorization header in WebSocket connect - blocking connection");
                throw new IllegalArgumentException("Missing JWT Token");
            }
        }
        return message;
    }
}