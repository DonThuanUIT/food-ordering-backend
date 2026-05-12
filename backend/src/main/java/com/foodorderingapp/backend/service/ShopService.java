package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ShopService {
    ShopResponse createShop(ShopCreateRequest request, String ownerPhone);
    List<ShopResponse> getVendorShops(String ownerPhone);
    Page<ShopResponse> getAllShops(Pageable pageable, String keyword);
    ShopDetailResponse getShopDetailWithMenu (UUID shopId);
}
