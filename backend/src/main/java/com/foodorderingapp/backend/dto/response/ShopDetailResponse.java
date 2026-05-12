package com.foodorderingapp.backend.dto.response;

import java.util.List;
import java.util.UUID;

public record ShopDetailResponse(
        UUID id,
        String name,
        String address,
        String description,
        List<CategoryMenuResponse> menu
) {
}
