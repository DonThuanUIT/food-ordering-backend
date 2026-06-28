package com.foodorderingapp.backend.modules.shop.controller;

import com.foodorderingapp.backend.modules.shop.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopLocationDTO;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopResponse;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shops") // API công khai cho Sinh viên/Khách (được permitAll ở SecurityConfig)
@RequiredArgsConstructor
public class ShopController {
    private final ShopService shopService;

    // 1. Lấy danh sách quán ăn (Phân trang + Tìm kiếm) cho Sinh viên
    @GetMapping
    public ResponseEntity<Page<ShopResponse>> getShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(shopService.getAllShops(pageable, keyword));
    }

    // 2. Lấy chi tiết quán và Menu (đã gom nhóm) cho Sinh viên
    @GetMapping("/{shopId}/detail-menu")
    public ResponseEntity<ShopDetailResponse> getShopDetailAndMenu(@PathVariable UUID shopId){
        return ResponseEntity.ok(shopService.getShopDetailWithMenu(shopId));
    }

    // 3. Toggle favorite shop
    @PostMapping("/{shopId}/favorite")
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable UUID shopId, Principal principal) {
        return ResponseEntity.ok(shopService.toggleFavoriteShop(shopId, principal.getName()));
    }

    // 4. Check if shop is favorite
    @GetMapping("/{shopId}/is-favorite")
    public ResponseEntity<Boolean> isFavorite(@PathVariable UUID shopId, Principal principal) {
        return ResponseEntity.ok(shopService.isFavoriteShop(shopId, principal.getName()));
    }

    // 5. Get favorite shops list
    @GetMapping("/favorite")
    public ResponseEntity<List<ShopResponse>> getFavoriteShops(Principal principal) {
        return ResponseEntity.ok(shopService.getFavoriteShops(principal.getName()));
    }

    // >>> PHASE 1: KTX Food Map - Lấy danh sách tọa độ quán ăn đang mở cửa
    @GetMapping("/locations")
    public ResponseEntity<List<ShopLocationDTO>> getShopLocations() {
        return ResponseEntity.ok(shopService.getActiveShopLocations());
    }
}
