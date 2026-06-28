package com.foodorderingapp.backend.modules.food.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.modules.building.BuildingService;
import com.foodorderingapp.backend.modules.building.dto.response.BuildingResponse;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopLocationDTO;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dịch vụ xử lý Tool/Function Calling từ Gemini.
 * Mỗi method là 1 "tool" mà Gemini có thể gọi (READ-ONLY).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionCallService {

    private final ShopService shopService;
    private final BuildingService buildingService;
    private final FoodRepository foodRepository;
    private final ObjectMapper objectMapper;

    /**
     * Tool 1: searchShops - Tìm quán ăn
     * Parameters: keyword (string, optional), lat (number, optional), lng (number, optional), radiusKm (number, optional, default 2.0)
     */
    public Map<String, Object> searchShops(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        Double lat = args.get("lat") != null ? ((Number) args.get("lat")).doubleValue() : null;
        Double lng = args.get("lng") != null ? ((Number) args.get("lng")).doubleValue() : null;
        Double radiusKm = args.get("radiusKm") != null ? ((Number) args.get("radiusKm")).doubleValue() : 2.0;

        log.info("FunctionCall: searchShops(keyword={}, lat={}, lng={}, radiusKm={})", keyword, lat, lng, radiusKm);

        // Nếu có tọa độ → tìm trong bán kính
        if (lat != null && lng != null) {
            List<ShopLocationDTO> nearby = shopService.findNearestShops(lat, lng, radiusKm);
            // Nếu có keyword thì filter tiếp
            if (keyword != null && !keyword.isBlank()) {
                String kw = keyword.toLowerCase();
                nearby = nearby.stream()
                        .filter(s -> s.getName().toLowerCase().contains(kw)
                                || (s.getAddress() != null && s.getAddress().toLowerCase().contains(kw)))
                        .collect(Collectors.toList());
            }
            return Map.of("shops", nearby, "count", nearby.size(), "nearby", true);
        }

        // Không có tọa độ → trả danh sách tất cả
        List<ShopLocationDTO> allShops = shopService.getActiveShopLocations();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            allShops = allShops.stream()
                    .filter(s -> s.getName().toLowerCase().contains(kw)
                            || (s.getAddress() != null && s.getAddress().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        return Map.of("shops", allShops, "count", allShops.size(), "nearby", false);
    }

    /**
     * Tool 2: searchFoods - Tìm món ăn
     * Parameters: keyword (string), shopId (string/UUID, optional), sortBy (string, optional)
     * sortBy: "price_asc" | "price_desc" | "name" | "rating"
     */
    public Map<String, Object> searchFoods(Map<String, Object> args) {
        String keyword = (String) args.get("keyword");
        String sortBy = (String) args.get("sortBy");

        log.info("FunctionCall: searchFoods(keyword={}, sortBy={})", keyword, sortBy);

        List<Food> foods;
        if (keyword != null && !keyword.isBlank()) {
            foods = foodRepository.searchAvailableFoodsByKeyword(keyword.toLowerCase());
        } else {
            foods = foodRepository.findAllAvailableFoods();
        }

        // Sort
        if (sortBy != null) {
            Comparator<Food> comparator;
            switch (sortBy) {
                case "price_asc":
                    comparator = Comparator.comparing(f -> f.getPrice() != null ? f.getPrice() : BigDecimal.ZERO);
                    foods.sort(comparator);
                    break;
                case "price_desc":
                    comparator = Comparator.comparing((Food f) -> f.getPrice() != null ? f.getPrice() : BigDecimal.ZERO);
                    foods.sort(comparator.reversed());
                    break;
                case "name":
                    foods.sort(Comparator.comparing(Food::getName));
                    break;
                // rating không có sẵn trong entity Food, bỏ qua
                default:
                    break;
            }
        }

        // Giới hạn tối đa 20 món để tránh token quá lớn
        if (foods.size() > 20) {
            foods = foods.subList(0, 20);
        }

        List<Map<String, Object>> foodList = foods.stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", f.getId().toString());
            map.put("name", f.getName());
            map.put("price", f.getPrice());
            map.put("description", f.getDescription());
            map.put("tags", f.getTags());
            map.put("cuisine", f.getCuisine());
            map.put("spicyLevel", f.getSpicyLevel());
            map.put("isAvailable", f.getIsAvailable());
            map.put("shopName", f.getShop() != null ? f.getShop().getName() : null);
            map.put("shopId", f.getShop() != null ? f.getShop().getId().toString() : null);
            return map;
        }).collect(Collectors.toList());

        return Map.of("foods", foodList, "count", foodList.size());
    }

    /**
     * Tool 3: getShopDetail - Lấy chi tiết quán + menu
     * Parameters: shopId (string/UUID)
     */
    public Map<String, Object> getShopDetail(Map<String, Object> args) {
        String shopIdStr = (String) args.get("shopId");
        log.info("FunctionCall: getShopDetail(shopId={})", shopIdStr);

        if (shopIdStr == null) {
            return Map.of("error", "shopId is required");
        }

        try {
            UUID shopId = UUID.fromString(shopIdStr);
            ShopDetailResponse detail = shopService.getShopDetailWithMenu(shopId);
            return objectMapper.convertValue(detail, Map.class);
        } catch (Exception e) {
            log.error("Error getting shop detail for {}: {}", shopIdStr, e.getMessage());
            return Map.of("error", "Shop not found or unavailable");
        }
    }

    /**
     * Tool 4: getBuildingCoordinates - Lấy tọa độ tòa nhà KTX
     * Parameters: name (string) - tên tòa nhà (VD: "A2", "B1", "C3")
     */
    public Map<String, Object> getBuildingCoordinates(Map<String, Object> args) {
        String name = (String) args.get("name");
        log.info("FunctionCall: getBuildingCoordinates(name={})", name);

        if (name == null || name.isBlank()) {
            return Map.of("error", "Building name is required");
        }

        Optional<BuildingResponse> building = buildingService.findByBuildingName(name.trim());
        if (building.isPresent()) {
            BuildingResponse b = building.get();
            return Map.of(
                    "name", b.getName(),
                    "latitude", b.getLatitude(),
                    "longitude", b.getLongitude(),
                    "found", true
            );
        }
        return Map.of("found", false, "message", "Không tìm thấy tòa nhà '" + name + "'");
    }

    /**
     * Tool 5: getRecommendedFoods - Gợi ý món ăn dựa trên query
     * Parameters: query (string), shopId (string/UUID, optional)
     */
    public Map<String, Object> getRecommendedFoods(Map<String, Object> args) {
        String query = (String) args.get("query");
        String shopIdStr = (String) args.get("shopId");
        log.info("FunctionCall: getRecommendedFoods(query={}, shopId={})", query, shopIdStr);

        if (query == null || query.isBlank()) {
            return Map.of("error", "Query is required");
        }

        return Map.of("message", "Hãy sử dụng API POST /api/ai/recommend với query=\"" + query + "\" để xem gợi ý chi tiết.");
    }
}