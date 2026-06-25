package com.foodorderingapp.backend.modules.food.dto.gemini;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiFoodAnalysis {
    private List<String> tags;
    private String cuisine;
    private Integer spicyLevel;
    private String suggestedDescription;
}
