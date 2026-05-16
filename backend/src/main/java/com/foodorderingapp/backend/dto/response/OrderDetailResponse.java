package com.foodorderingapp.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderDetailResponse {
    private String foodName;
    private BigDecimal price;
    private Integer quantity;
}
