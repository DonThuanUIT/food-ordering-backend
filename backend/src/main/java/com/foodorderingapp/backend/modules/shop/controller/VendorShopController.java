package com.foodorderingapp.backend.modules.shop.controller;

import com.foodorderingapp.backend.modules.shop.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopUpdateRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopResponse;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.modules.order.OrderService;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/vendor/shops")
@RequiredArgsConstructor
public class VendorShopController {

    private final ShopService shopService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ShopResponse> createShop(
            @Valid @RequestBody ShopCreateRequest request,
            Principal principal
    ) {
        String ownerPhone = principal.getName();
        ShopResponse response = shopService.createShop(request, ownerPhone);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getVendorShops(Principal principal) {
        String ownerPhone = principal.getName();
        List<ShopResponse> responses = shopService.getVendorShops(ownerPhone);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{shopId}/profile")
    public ResponseEntity<ShopResponse> updateShopProfile(
            @PathVariable UUID shopId,
            @Valid @RequestBody ShopUpdateRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        ShopResponse response = shopService.updateShopProfile(shopId, request, vendorPhone);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{shopId}/status")
    public ResponseEntity<ShopResponse> toggleShopStatus(
            @PathVariable UUID shopId,
            @RequestBody Map<String, Boolean> body, // Dùng Map hứng JSON { "isActive": true/false }
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        Boolean isActive = body.get("isActive");
        ShopResponse response = shopService.toggleShopStatus(shopId, isActive, vendorPhone);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{shopId}/orders")
    public ResponseEntity<List<OrderResponse>> getShopOrders(
            @PathVariable UUID shopId,
            @RequestParam(required = false) String status,
            Principal principal) {
        return ResponseEntity.ok(orderService.getVendorOrders(shopId, status));
    }
    @PatchMapping("/{shopId}/orders/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID shopId,
            @PathVariable UUID orderId,
            @RequestBody UpdateStatusRequest request,
            Principal principal) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }
}