package com.foodorderingapp.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class StudentReviewResponse {
    private UUID id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private UUID orderId;
    private String shopName;
    private BigDecimal totalPrice;
}