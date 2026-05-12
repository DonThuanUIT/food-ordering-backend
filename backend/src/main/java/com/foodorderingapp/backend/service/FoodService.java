package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.FoodRequest;
import com.foodorderingapp.backend.dto.response.FoodResponse;

import java.util.List;
import java.util.UUID;

public interface FoodService {
    List<FoodResponse> getAllFoods(UUID shopId, UUID categoryId,String vendorPhone);
    FoodResponse createFood(UUID shopId, FoodRequest request, String vendorPhone);
    FoodResponse updateFood(UUID shopId, UUID foodId, FoodRequest request, String vendorPhone);
    void deleteFood(UUID shopId, UUID foodId, String vendorPhone);

    FoodResponse toggleFoodAvailability(UUID shopId, UUID foodId, String vendorPhone);
}
