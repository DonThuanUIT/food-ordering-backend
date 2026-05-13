package com.foodorderingapp.backend.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID foodId,
        String foodName,
        String foodImageUrl,
        BigDecimal price,
        Integer quantity,
        String note
) {
}
