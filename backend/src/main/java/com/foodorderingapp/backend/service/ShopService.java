package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.ShopResponse;

import java.util.List;

public interface ShopService {
    ShopResponse createShop(ShopCreateRequest request, String ownerPhone);
    List<ShopResponse> getVendorShops(String ownerPhone);
}
