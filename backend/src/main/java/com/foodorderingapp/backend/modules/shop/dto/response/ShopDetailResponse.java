package com.foodorderingapp.backend.modules.shop.dto.response;

import com.foodorderingapp.backend.modules.food.dto.response.CategoryMenuResponse;

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