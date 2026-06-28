package com.foodorderingapp.backend.modules.food.dto.gemini;

import lombok.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequest {
    private List<Content> contents;
    private List<Tool> tools;
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
        private String role;
        private List<Part> parts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
        private InlineData inlineData;
        private FunctionCall functionCall;
        private FunctionResponse functionResponse;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InlineData {
        private String mimeType;
        private String data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionCall {
        private String name;
        private Map<String, Object> args;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionResponse {
        private String name;
        private Map<String, Object> response;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Tool {
        private List<FunctionDeclaration> functionDeclarations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FunctionDeclaration {
        private String name;
        private String description;
        private OpenApiSchema parameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenApiSchema {
        private String type;
        private Map<String, SchemaProperty> properties;
        private List<String> required;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchemaProperty {
        private String type;
        private String description;
        private String format;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenerationConfig {
        private String responseMimeType;
    }
}