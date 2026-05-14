package com.foodorderingapp.backend.component;

import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShopValidationComponent {

    private final ShopRepository shopRepository;

    public Shop validateAndGetShop(UUID shopId, String vendorPhone) {
        return shopRepository.findByIdAndOwner_Phone(shopId, vendorPhone)
                .orElseThrow(() -> new AppException("Bạn không có quyền truy cập quán ăn này hoặc quán không tồn tại!", HttpStatus.FORBIDDEN));
    }
}