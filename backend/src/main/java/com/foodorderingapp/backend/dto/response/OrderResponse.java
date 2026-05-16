package com.foodorderingapp.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String shopName;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalPrice;
    private String status;
    private String building;
    private String dropOff;
    private String cancelReason;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> details;
}
