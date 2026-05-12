package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShopService {
    ShopResponse createShop(ShopCreateRequest request, String ownerPhone);
    List<ShopResponse> getVendorShops(String ownerPhone);
    Page<ShopResponse> getAllShops(Pageable pageable, String keyword);
}
