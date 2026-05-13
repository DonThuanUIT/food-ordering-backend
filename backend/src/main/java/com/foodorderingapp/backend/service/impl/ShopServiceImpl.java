package com.foodorderingapp.backend.service.impl;

import com.foodorderingapp.backend.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.dto.response.CategoryMenuResponse;
import com.foodorderingapp.backend.dto.response.FoodResponse;
import com.foodorderingapp.backend.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.dto.response.ShopResponse;
import com.foodorderingapp.backend.entity.Category;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.entity.enums.ShopStatus;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.FoodRepository;
import com.foodorderingapp.backend.repository.ShopRepository;
import com.foodorderingapp.backend.repository.UserRepository;
import com.foodorderingapp.backend.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;

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
                .address(request.getAddress())
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
                .address(shop.getAddress())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .status(shop.getStatus().name())
                .isActive(shop.getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getAllShops(Pageable pageable, String keyword) {

        Page<Shop> shopPage;
        if(keyword != null && !keyword.trim().isEmpty()){
            shopPage = shopRepository.searchShops(keyword, ShopStatus.APPROVED, pageable);
        } else {
            shopPage = shopRepository.findAllByStatusAndIsActiveTrue(ShopStatus.APPROVED, pageable);
        }
        List<ShopResponse> content = new ArrayList<>(shopPage.getContent().stream()
                .map(this::mapToStudentResponse)
                .toList());
        content.sort((s1, s2) -> {
            if (s1.getDisplayStatus().equals(s2.getDisplayStatus())) return 0;
            return s1.getDisplayStatus().equals("OPENING") ? -1 : 1;
        });
        return new PageImpl<>(content, pageable, shopPage.getTotalElements());

    }

    private ShopResponse mapToStudentResponse(Shop shop) {
        LocalTime now = LocalTime.now();
        String displayStatus = "CLOSED";
        if (shop.getOpenTime() != null && shop.getCloseTime() != null) {
            if (shop.getCloseTime().isAfter(shop.getOpenTime())) {
                if (now.isAfter(shop.getOpenTime()) && now.isBefore(shop.getCloseTime())) {
                    displayStatus = "OPENING";
                }
            }
        } else {
            if(now.isAfter(shop.getOpenTime()) || now.isBefore(shop.getCloseTime())) {
                displayStatus = "OPENING";
            }
        }
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .status(shop.getStatus().name())
                .isActive(shop.getIsActive())
                .displayStatus(displayStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopDetailResponse getShopDetailWithMenu(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Khong tim thay cua hang voi ID: " + shopId, HttpStatus.NOT_FOUND));

        List<Food> foods = foodRepository.findAllByShopIdWithCategory(shopId);

        // 1. Gom nhóm món ăn theo Category
        Map<Category, List<Food>> grouped = foods.stream().collect(Collectors.groupingBy(Food::getCategory));

        // 2. Lặp qua từng nhóm (entry) để chuyển đổi dữ liệu
        List<CategoryMenuResponse> menu = grouped.entrySet().stream()
                .map(entry -> {
                    Category cat = entry.getKey(); // Lấy thông tin Category

                    // Chuyển List<Food> thành List<FoodResponse> bằng Builder của bạn
                    List<FoodResponse> foodResponses = entry.getValue().stream()
                            .map(f -> FoodResponse.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .price(f.getPrice())
                                    .isAvailable(f.getIsAvailable())
                                    .imageUrl(f.getImageUrl())
                                    .description(f.getDescription())
                                    .build()
                            ).toList();

                    // Đóng gói thành CategoryMenuResponse
                    return new CategoryMenuResponse(cat.getId(), cat.getName(), foodResponses);
                }).toList();

        // 3. Trả về kết quả cuối cùng
        return new ShopDetailResponse(
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                shop.getDescription(),
                menu
        );
    }

}