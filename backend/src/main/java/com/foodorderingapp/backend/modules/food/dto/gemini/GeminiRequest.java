package com.foodorderingapp.backend.modules.food.dto.gemini;

import lombok.*;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    private List<Content> contents;
    private GenerationConfig generationConfig;

    public static GeminiRequest fromPrompt(String promptText, boolean requireJson) {
        Part part = Part.builder().text(promptText).build();
        Content content = Content.builder().parts(Collections.singletonList(part)).build();
        
        GeminiRequest request = GeminiRequest.builder()
                .contents(Collections.singletonList(content))
                .build();
                
        if (requireJson) {
            request.setGenerationConfig(GenerationConfig.builder()
                    .responseMimeType("application/json")
                    .build());
        }
        return request;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        private String responseMimeType;
    }
}
