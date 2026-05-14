package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import com.foodorderingapp.backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}