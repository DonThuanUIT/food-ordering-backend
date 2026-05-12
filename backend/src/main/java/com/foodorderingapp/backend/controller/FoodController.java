package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.FoodRequest;
import com.foodorderingapp.backend.dto.response.FoodResponse;
import com.foodorderingapp.backend.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendor/shops/{shopId}/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAllFoods(
            @PathVariable UUID shopId,
            @RequestParam(required = false) UUID categoryId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        return ResponseEntity.ok(foodService.getAllFoods(shopId, categoryId, vendorPhone));
    }

    @PostMapping
    public ResponseEntity<FoodResponse> createFood(
            @PathVariable UUID shopId,
            @Valid @RequestBody FoodRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        FoodResponse response = foodService.createFood(shopId, request, vendorPhone);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{foodId}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable UUID shopId,
            @PathVariable UUID foodId,
            @Valid @RequestBody FoodRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        return ResponseEntity.ok(foodService.updateFood(shopId, foodId, request, vendorPhone));
    }

    @DeleteMapping("/{foodId}")
    public ResponseEntity<Void> deleteFood(
            @PathVariable UUID shopId,
            @PathVariable UUID foodId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        foodService.deleteFood(shopId, foodId, vendorPhone);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{foodId}/toggle")
    public ResponseEntity<FoodResponse> toggleAvailability(
            @PathVariable UUID shopId,
            @PathVariable UUID foodId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        return ResponseEntity.ok(foodService.toggleFoodAvailability(shopId, foodId, vendorPhone));
    }
}