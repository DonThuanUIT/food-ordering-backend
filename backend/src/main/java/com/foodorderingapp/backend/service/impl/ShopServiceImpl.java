package com.foodorderingapp.backend.service.impl;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.entity.enums.ShopStatus;
import com.foodorderingapp.backend.repository.ShopRepository;
import com.foodorderingapp.backend.repository.UserRepository;
import com.foodorderingapp.backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ShopResponse createShop(ShopCreateRequest request, String ownerPhone) {
        User owner = userRepository.findByPhone(ownerPhone)
                .orElseThrow(() -> new RuntimeException("No owner information found"));

        if (shopRepository.existsByNameAndOwnerId(request.getName(), owner.getId())) {
            throw new RuntimeException("There is already a shop with this name");
        }

        Shop newShop = Shop.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(ShopStatus.PENDING)
                .isActive(true)
                .build();

        Shop savedShop = shopRepository.save(newShop);
        log.info("{} vendor has just created a new shop : {}", ownerPhone, savedShop.getName());

        return mapToResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getVendorShops(String ownerPhone) {
        User owner = userRepository.findByPhone(ownerPhone)
                .orElseThrow(() -> new RuntimeException("No one owner information found"));

        List<Shop> shops = shopRepository.findAllByOwnerId(owner.getId());

        return shops.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ShopResponse mapToResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .status(shop.getStatus().name())
                .isActive(shop.getIsActive())
                .build();
    }
}