package com.foodorderingapp.backend.modules.chat.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatMessageResponse {
    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String content;
    private LocalDateTime createdAt;
}