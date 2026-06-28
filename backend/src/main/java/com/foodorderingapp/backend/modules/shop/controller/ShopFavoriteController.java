package com.foodorderingapp.backend.modules.shop.controller;

import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.ShopFollower;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopFollowerRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/shop-favorites")
@RequiredArgsConstructor
public class ShopFavoriteController {

    private final ShopFollowerRepository shopFollowerRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @GetMapping("/shops/{shopId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> isFavorite(@PathVariable UUID shopId, Principal principal) {
        return ResponseEntity.ok(shopFollowerRepository.existsByUserPhoneAndShopId(principal.getName(), shopId));
    }

    @PostMapping("/shops/{shopId}/toggle")
    @Transactional
    public ResponseEntity<Boolean> toggleFavorite(@PathVariable UUID shopId, Principal principal) {
        User user = userRepository.findByPhone(principal.getName())
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new AppException("Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN);
        }

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Không tìm thấy quán ăn", HttpStatus.NOT_FOUND));
        if (!isVisibleShop(shop)) {
            throw new AppException("Quán ăn hiện không khả dụng", HttpStatus.BAD_REQUEST);
        }

        return shopFollowerRepository.findByUserPhoneAndShopId(principal.getName(), shopId)
                .map(existing -> {
                    shopFollowerRepository.delete(existing);
                    return ResponseEntity.ok(false);
                })
                .orElseGet(() -> {
                    shopFollowerRepository.save(ShopFollower.builder()
                            .user(user)
                            .shop(shop)
                            .build());
                    return ResponseEntity.ok(true);
                });
    }

    private boolean isVisibleShop(Shop shop) {
        return shop.getStatus() == ShopStatus.APPROVED
                && Boolean.TRUE.equals(shop.getIsActive())
                && (shop.getOwner() == null || !Boolean.TRUE.equals(shop.getOwner().getIsLocked()));
    }
}
