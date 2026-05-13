package com.foodorderingapp.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<ShopCartResponse> shops,
        BigDecimal totalAmount
) {
}
