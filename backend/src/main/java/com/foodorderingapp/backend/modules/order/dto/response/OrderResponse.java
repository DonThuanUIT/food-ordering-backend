package com.foodorderingapp.backend.modules.order.dto.response;

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
    private String displayStatus;
    private String building;
    private String cancelReason;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> details;
    private String voucherCode;
    private BigDecimal discountAmount;
    private Boolean isReviewed;
    
    private UUID shipperId;
    private String shipperName;
    private String shipperPhone;
    private Double shipperLatitude;
    private Double shipperLongitude;

    private UUID shopId;
    private String shopAddress;
    private Double shopLatitude;
    private Double shopLongitude;
    private Double buildingLatitude;
    private Double buildingLongitude;
}
