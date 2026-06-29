package com.foodorderingapp.backend.modules.food.controller;

import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.modules.food.dto.gemini.GeminiRecommendationMatch;
import com.foodorderingapp.backend.modules.food.dto.gemini.GeminiRequest;
import com.foodorderingapp.backend.modules.food.dto.gemini.GeminiResponse;
import com.foodorderingapp.backend.modules.food.dto.request.AIRecommendationRequest;
import com.foodorderingapp.backend.modules.food.dto.response.AIRecommendationResponse;
import com.foodorderingapp.backend.modules.food.dto.response.FoodResponse;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.food.service.FunctionCallService;
import com.foodorderingapp.backend.modules.food.service.GeminiService;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopLocationDTO;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import com.foodorderingapp.backend.modules.building.BuildingService;
import com.foodorderingapp.backend.modules.building.dto.response.BuildingResponse;
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
    private final FunctionCallService functionCallService;
    private final ShopService shopService;
    private final BuildingService buildingService;

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

    // >>> PHASE 1: AI Spatial - Tìm quán ăn gần vị trí người dùng
    @PostMapping("/nearby")
    public ResponseEntity<List<ShopLocationDTO>> findNearbyShops(
            @RequestParam double userLat,
            @RequestParam double userLng,
            @RequestParam(defaultValue = "2.0") double radiusKm) {
        log.info("AI Spatial request: nearby shops from ({}, {}) within {}km", userLat, userLng, radiusKm);
        return ResponseEntity.ok(shopService.findNearestShops(userLat, userLng, radiusKm));
    }

    // >>> PHASE 1: AI Spatial - Tra cứu tọa độ tòa nhà theo tên
    @GetMapping("/building")
    public ResponseEntity<BuildingResponse> findBuilding(@RequestParam String name) {
        return buildingService.findByBuildingName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // >>> PHASE 2: Gemini Native Function Calling - Agentic Chat
    @PostMapping("/agent/chat")
    public ResponseEntity<Map<String, Object>> agenticChat(
            @Valid @RequestBody AIRecommendationRequest request) {
        log.info("Agentic Chat request: query='{}', userLat={}, userLng={}, buildingName='{}'",
                request.getQuery(), request.getUserLat(), request.getUserLng(), request.getBuildingName());

        // 1. Xây dựng System Prompt
        String systemPrompt = "Bạn là trợ lý ảo thông minh của ứng dụng đặt đồ ăn KTX. " +
                "Nhiệm vụ của bạn là hỗ trợ sinh viên tìm quán, tìm món, tra cứu thông tin. " +
                "Bạn CÓ THỂ gọi các công cụ (functions) để tra cứu dữ liệu thực tế từ hệ thống. " +
                "Hãy phân tích câu hỏi của sinh viên và quyết định công cụ phù hợp nhất để trả lời. " +
                "Nếu sinh viên hỏi về quán gần, hãy sử dụng searchShops với tham số lat/lng. " +
                "Nếu sinh viên hỏi về món ăn, hãy sử dụng searchFoods. " +
                "Nếu sinh viên hỏi về tòa nhà KTX, hãy sử dụng getBuildingCoordinates. " +
                "Sau khi nhận được kết quả từ công cụ, hãy tổng hợp và trả lời bằng tiếng Việt tự nhiên, thân thiện.";

        // Thêm context vị trí nếu có (chỉ khi userLat/userLng != null)
        String userMessage = "";
        if (request.getUserLat() != null && request.getUserLng() != null) {
            userMessage = String.format(
                    "Vị trí hiện tại của sinh viên: latitude=%.6f, longitude=%.6f.",
                    request.getUserLat(), request.getUserLng());
            if (request.getBuildingName() != null && !request.getBuildingName().isBlank()) {
                userMessage += String.format(" Sinh viên đang ở tòa %s.", request.getBuildingName());
            }
            userMessage += "\n";
        }

        userMessage += "Câu hỏi của sinh viên: " + request.getQuery();

        // 2. Xây dựng request với tools declaration
        GeminiRequest.Content sysContent = GeminiRequest.Content.builder()
                .role("user")
                .parts(List.of(GeminiRequest.Part.builder().text(systemPrompt + "\n\n" + userMessage).build()))
                .build();

        GeminiRequest geminiRequest = GeminiRequest.builder()
                .contents(List.of(sysContent))
                .tools(geminiService.createToolsDeclaration())
                .build();

        // 3. Agentic loop - tối đa 3 vòng lặp
        int maxIterations = 3;
        for (int i = 0; i < maxIterations; i++) {
            GeminiResponse geminiResponse = geminiService.callGeminiWithFunctions(geminiRequest);

            if (geminiResponse == null || geminiResponse.getCandidates() == null || geminiResponse.getCandidates().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "response", "Xin lỗi, tôi không thể xử lý yêu cầu của bạn ngay lúc này. Vui lòng thử lại sau.",
                        "type", "error"
                ));
            }

            GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);
            if (candidate.getContent() == null || candidate.getContent().getParts() == null) {
                break;
            }

            List<GeminiResponse.Part> parts = candidate.getContent().getParts();
            boolean hasFunctionCall = false;

            // 4. Kiểm tra xem Gemini có yêu cầu function_call không
            for (GeminiResponse.Part part : parts) {
                if (part.getFunctionCall() != null) {
                    hasFunctionCall = true;
                    String functionName = part.getFunctionCall().getName();
                    Map<String, Object> functionArgs = part.getFunctionCall().getArgs() != null
                            ? part.getFunctionCall().getArgs()
                            : new HashMap<>();

                    log.info("Gemini requested function: {} with args: {}", functionName, functionArgs);

                    // Thực thi tool
                    Map<String, Object> result;
                    try {
                        result = switch (functionName) {
                            case "searchShops" -> functionCallService.searchShops(functionArgs);
                            case "searchFoods" -> functionCallService.searchFoods(functionArgs);
                            case "getShopDetail" -> functionCallService.getShopDetail(functionArgs);
                            case "getBuildingCoordinates" -> functionCallService.getBuildingCoordinates(functionArgs);
                            default -> Map.of("error", "Unknown function: " + functionName);
                        };
                    } catch (Exception e) {
                        log.error("Error executing function {}: {}", functionName, e.getMessage());
                        result = Map.of("error", "Lỗi khi thực thi: " + e.getMessage());
                    }

                    // Thêm kết quả function vào history cho Gemini
                    List<GeminiRequest.Part> updatedParts = new ArrayList<>();
                    if (geminiRequest.getContents() != null && !geminiRequest.getContents().isEmpty()) {
                        List<GeminiRequest.Part> existingParts = geminiRequest.getContents().get(0).getParts();
                        if (existingParts != null) {
                            updatedParts.addAll(existingParts);
                        }
                    }
                    updatedParts.add(GeminiRequest.Part.builder()
                            .functionCall(GeminiRequest.FunctionCall.builder()
                                    .name(functionName)
                                    .args(functionArgs)
                                    .build())
                            .build());
                    updatedParts.add(GeminiRequest.Part.builder()
                            .functionResponse(GeminiRequest.FunctionResponse.builder()
                                    .name(functionName)
                                    .response(result)
                                    .build())
                            .build());

                    geminiRequest.setContents(List.of(
                            GeminiRequest.Content.builder()
                                    .role("user")
                                    .parts(updatedParts)
                                    .build()
                    ));
                    break;
                }
            }

            // Nếu không có function_call, Gemini đã trả lời text → kết thúc
            if (!hasFunctionCall) {
                String textResponse = parts.stream()
                        .filter(p -> p.getText() != null)
                        .map(GeminiResponse.Part::getText)
                        .reduce("", (a, b) -> a + b);

                if (textResponse.isBlank()) {
                    textResponse = "Xin lỗi, tôi không tìm thấy thông tin phù hợp cho yêu cầu của bạn.";
                }

                // Gợi ý bật GPS nếu user chưa cung cấp tọa độ
                boolean suggestGps = request.getUserLat() == null || request.getUserLng() == null;

                return ResponseEntity.ok(Map.of(
                        "response", textResponse,
                        "type", "text",
                        "suggestGps", suggestGps
                ));
            }
        }

        // Fallback nếu loop kết thúc mà không có text response
        return ResponseEntity.ok(Map.of(
                "response", "Xin lỗi, tôi không thể xử lý yêu cầu phức tạp này. Vui lòng thử lại với câu hỏi khác.",
                "type", "text",
                "suggestGps", true
        ));
    }

    private List<Food> getKeywordFallbackMatches(String query, List<Food> availableFoods) {
        String queryLower = query.toLowerCase();
        String[] keywords = queryLower.split("\\s+");
        
        List<FoodScore> scoredFoods = new ArrayList<>();
        for (Food food : availableFoods) {
            int score = 0;
            String foodNameLower = food.getName().toLowerCase();
            String foodDescLower = food.getDescription() != null ? food.getDescription().toLowerCase() : "";
            
            for (String kw : keywords) {
                if (kw.length() < 2) continue;
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

    /**
     * Admin endpoint to batch re-analyze all foods that have fewer than 5 tags.
     * This calls Gemini sequentially with delays to respect rate limits.
     */
    @PostMapping("/admin/reanalyze-untagged")
    public ResponseEntity<Map<String, Object>> reanalyzeUntaggedFoods() {
        log.info("Admin triggered batch re-analysis of foods with fewer than 5 tags");
        Map<String, Object> result = geminiService.reanalyzeFoodsWithFewTags();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/admin/reanalyze-all-accurate")
    public ResponseEntity<Map<String, Object>> reanalyzeAllFoodsAccurate() {
        log.info("Admin triggered accurate batch re-analysis for all foods in database");
        Map<String, Object> result = geminiService.reanalyzeAllFoodsAccurate();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/inspect-tags")
    public ResponseEntity<List<Map<String, Object>>> inspectTags() {
        List<Food> foods = foodRepository.findAll();
        List<Map<String, Object>> response = foods.stream().map(f -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", f.getId());
            map.put("name", f.getName());
            map.put("tags", f.getTags());
            map.put("cuisine", f.getCuisine());
            map.put("spicyLevel", f.getSpicyLevel());
            map.put("description", f.getDescription());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
