package com.foodorderingapp.backend.modules.food.controller;

import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.modules.food.dto.gemini.GeminiRecommendationMatch;
import com.foodorderingapp.backend.modules.food.dto.request.AIRecommendationRequest;
import com.foodorderingapp.backend.modules.food.dto.response.AIRecommendationResponse;
import com.foodorderingapp.backend.modules.food.dto.response.FoodResponse;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.food.service.GeminiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIRecommendationController {

    private final FoodRepository foodRepository;
    private final GeminiService geminiService;

    @PostMapping("/recommend")
    public ResponseEntity<List<AIRecommendationResponse>> getRecommendations(
            @Valid @RequestBody AIRecommendationRequest request,
            @RequestParam(required = false) UUID shopId
    ) {
        log.info("Received AI recommendation request: query='{}', shopId={}", request.getQuery(), shopId);

        // 1. Lấy danh sách món ăn đang bán
        List<Food> availableFoods = (shopId != null) 
                ? foodRepository.findAllAvailableFoodsByShopId(shopId) 
                : foodRepository.findAllAvailableFoods();

        if (availableFoods.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 2. Gọi Gemini để phân tích và đề xuất
        List<GeminiRecommendationMatch> matches = geminiService.recommendFoods(request.getQuery(), availableFoods);
        List<AIRecommendationResponse> responses = new ArrayList<>();

        if (matches != null && !matches.isEmpty()) {
            // Map từ kết quả so khớp của Gemini sang response DTO
            Map<UUID, Food> foodMap = availableFoods.stream()
                    .collect(Collectors.toMap(Food::getId, f -> f, (f1, f2) -> f1));

            for (GeminiRecommendationMatch match : matches) {
                Food food = foodMap.get(match.getFoodId());
                if (food != null) {
                    responses.add(AIRecommendationResponse.builder()
                            .food(mapToResponse(food))
                            .reason(match.getReason())
                            .build());
                }
            }
        }

        // 3. Cơ chế Fallback nếu Gemini không trả về kết quả (mất mạng, rate limit 429...)
        if (responses.isEmpty()) {
            log.info("Gemini recommendation returned no results or failed. Using keyword-based fallback.");
            List<Food> fallbackMatches = getKeywordFallbackMatches(request.getQuery(), availableFoods);
            for (Food food : fallbackMatches) {
                responses.add(AIRecommendationResponse.builder()
                        .food(mapToResponse(food))
                        .reason("Món ăn này phù hợp với từ khóa tìm kiếm của bạn.")
                        .build());
            }
        }

        return ResponseEntity.ok(responses);
    }

    private List<Food> getKeywordFallbackMatches(String query, List<Food> availableFoods) {
        String queryLower = query.toLowerCase();
        // Cắt từ khóa tìm kiếm
        String[] keywords = queryLower.split("\\s+");
        
        List<FoodScore> scoredFoods = new ArrayList<>();
        for (Food food : availableFoods) {
            int score = 0;
            String foodNameLower = food.getName().toLowerCase();
            String foodDescLower = food.getDescription() != null ? food.getDescription().toLowerCase() : "";
            
            // So khớp từ khóa với tên món ăn (được nhiều điểm nhất)
            for (String kw : keywords) {
                if (kw.length() < 2) continue; // Bỏ qua từ quá ngắn
                if (foodNameLower.contains(kw)) {
                    score += 10;
                }
                if (foodDescLower.contains(kw)) {
                    score += 2;
                }
                if (food.getTags() != null) {
                    for (String tag : food.getTags()) {
                        if (tag.toLowerCase().contains(kw)) {
                            score += 5;
                        }
                    }
                }
                if (food.getCuisine() != null && food.getCuisine().toLowerCase().contains(kw)) {
                    score += 5;
                }
            }
            if (score > 0) {
                scoredFoods.add(new FoodScore(food, score));
            }
        }

        // Sắp xếp giảm dần theo điểm và lấy tối đa 3 món
        return scoredFoods.stream()
                .sorted(Comparator.comparingInt(FoodScore::getScore).reversed())
                .limit(3)
                .map(FoodScore::getFood)
                .collect(Collectors.toList());
    }

    private FoodResponse mapToResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .imageUrl(food.getImageUrl())
                .isAvailable(food.getIsAvailable())
                .categoryId(food.getCategory().getId())
                .categoryName(food.getCategory().getName())
                .tags(food.getTags())
                .cuisine(food.getCuisine())
                .spicyLevel(food.getSpicyLevel())
                .build();
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    private static class FoodScore {
        private final Food food;
        private final int score;
    }
}
