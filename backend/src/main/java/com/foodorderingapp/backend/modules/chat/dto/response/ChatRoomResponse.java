package com.foodorderingapp.backend.modules.chat.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatRoomResponse {
    private UUID roomId;
    private UUID partnerId; // ID của đối phương (ShopId hoặc UserId)
    private String partnerName; // Tên đối phương để hiển thị
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
