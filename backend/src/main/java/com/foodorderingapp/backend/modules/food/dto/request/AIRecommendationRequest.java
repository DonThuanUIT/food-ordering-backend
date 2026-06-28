package com.foodorderingapp.backend.modules.food.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRecommendationRequest {
    @NotBlank(message = "Yêu cầu gợi ý không được để trống")
    private String query;

    // >>> PHASE 1: AI Spatial - Các trường tùy chọn (nullable), không ép buộc
    private Double userLat;
    private Double userLng;
    private String buildingName;
}
