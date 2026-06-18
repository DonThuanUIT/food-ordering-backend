package com.foodorderingapp.backend.modules.shop.service;

import com.foodorderingapp.backend.modules.shop.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopUpdateRequest;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopResponse;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ShopService {
    ShopResponse createShop(ShopCreateRequest request, String ownerPhone);
    List<ShopResponse> getVendorShops(String ownerPhone);
    Page<ShopResponse> getAllShops(Pageable pageable, String keyword);
    ShopDetailResponse getShopDetailWithMenu (UUID shopId);
    ShopResponse updateShopProfile(UUID shopId, ShopUpdateRequest request, String vendorPhone);
    ShopResponse toggleShopStatus(UUID shopId, java.util.Map<String, Boolean> statusMap, String vendorPhone);
    ShopResponse getVendorShopById(UUID shopId, String vendorPhone);

    Page<ShopResponse> getShopsForAdmin(ShopStatus status, Pageable pageable);
    ShopResponse updateShopStatus(UUID shopId, ShopStatus status);
}
