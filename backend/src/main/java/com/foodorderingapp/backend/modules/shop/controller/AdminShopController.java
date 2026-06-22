package com.foodorderingapp.backend.modules.shop.controller;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopResponse;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/shops")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    // [BE-Task 1] Lấy danh sách quán chờ duyệt hoặc theo trạng thái
    @GetMapping
    public ResponseEntity<Page<ShopResponse>> getShopsForAdmin(
            @RequestParam(required = false, defaultValue = "PENDING") ShopStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(shopService.getShopsForAdmin(status, pageable));
    }

    // [BE-Task 2] Duyệt (APPROVED) hoặc Từ chối (REJECTED) quán
    @PatchMapping("/{shopId}/status")
    public ResponseEntity<ShopResponse> updateShopStatus(
            @PathVariable UUID shopId,
            @RequestBody Map<String, String> body
    ) {
        ShopStatus newStatus = ShopStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(shopService.updateShopStatus(shopId, newStatus));
    }
    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponse> getShopDetailForAdmin(@PathVariable UUID shopId) {
        ShopResponse shopDetail = shopService.getShopDetailForAdmin(shopId);
        return ResponseEntity.ok(shopDetail);
    }
}
