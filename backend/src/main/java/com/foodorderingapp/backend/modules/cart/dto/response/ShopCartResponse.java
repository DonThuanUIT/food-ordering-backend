package com.foodorderingapp.backend.modules.cart.dto.response;

import java.util.List;
import java.util.UUID;

public record ShopCartResponse(
        UUID shopId,
        String shopName,
        List<CartItemResponse> items
) {
}
