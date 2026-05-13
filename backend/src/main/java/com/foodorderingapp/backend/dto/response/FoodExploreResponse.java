package com.foodorderingapp.backend.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FoodExploreResponse(
        UUID id,
        String foodName,
        BigDecimal price,
        String foodImageUrl,
        String description,
        UUID shopId,
        String shopName,
        String categoryName
) {
}
