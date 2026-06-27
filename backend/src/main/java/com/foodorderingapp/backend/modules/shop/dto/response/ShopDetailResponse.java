package com.foodorderingapp.backend.modules.shop.dto.response;

import com.foodorderingapp.backend.modules.food.dto.response.CategoryMenuResponse;

import java.util.List;
import java.time.LocalTime;
import java.util.UUID;

public record ShopDetailResponse(
        UUID id,
        String name,
        String address,
        String description,
        String coverUrl,
        String logoUrl,
        LocalTime openTime,
        LocalTime closeTime,
        Boolean isOpen,
        Double latitude,
        Double longitude,
        List<CategoryMenuResponse> menu
) {
}
