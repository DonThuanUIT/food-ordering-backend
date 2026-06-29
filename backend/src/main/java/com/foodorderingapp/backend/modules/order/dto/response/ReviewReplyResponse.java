package com.foodorderingapp.backend.modules.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewReplyResponse {
    private UUID id;
    private UUID reviewId;
    private String replyText;
    private String senderName;
    private String senderRole; // "VENDOR" or "STUDENT"
    private LocalDateTime createdAt;
}
