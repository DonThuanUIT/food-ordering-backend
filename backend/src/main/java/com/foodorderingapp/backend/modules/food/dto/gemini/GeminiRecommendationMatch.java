package com.foodorderingapp.backend.modules.food.dto.gemini;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRecommendationMatch {
    private UUID foodId;
    private String reason;
}
