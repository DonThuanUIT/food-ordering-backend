package com.foodorderingapp.backend.modules.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<ShopCartResponse> shops,
        BigDecimal totalAmount
) {
}
