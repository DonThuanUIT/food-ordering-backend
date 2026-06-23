package com.foodorderingapp.backend.modules.food.service;

import com.foodorderingapp.backend.modules.food.dto.response.FoodExploreResponse;
import com.foodorderingapp.backend.modules.food.dto.request.FoodRequest;
import com.foodorderingapp.backend.modules.food.dto.response.FoodResponse;

import java.util.List;
import java.util.UUID;

public interface FoodService {
    org.springframework.data.domain.Page<FoodExploreResponse> getExploreFoods(org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<FoodResponse> getAllFoods(UUID shopId, UUID categoryId, String vendorPhone, org.springframework.data.domain.Pageable pageable);
    FoodResponse createFood(UUID shopId, FoodRequest request, String vendorPhone);
    FoodResponse updateFood(UUID shopId, UUID foodId, FoodRequest request, String vendorPhone);
    void deleteFood(UUID shopId, UUID foodId, String vendorPhone);

    FoodResponse toggleFoodAvailability(UUID shopId, UUID foodId, String vendorPhone);
}
