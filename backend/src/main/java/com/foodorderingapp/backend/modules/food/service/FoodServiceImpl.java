package com.foodorderingapp.backend.modules.food.service;

import com.foodorderingapp.backend.modules.food.dto.request.FoodRequest;
import com.foodorderingapp.backend.modules.food.dto.response.FoodResponse;
import com.foodorderingapp.backend.entity.Category;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.food.repository.CategoryRepository;
import com.foodorderingapp.backend.modules.food.dto.response.FoodExploreResponse;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import com.foodorderingapp.backend.modules.food.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final GeminiService geminiService;

    private Shop validateShopOwnership(UUID shopId, String vendorPhone) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Cửa hàng không tồn tại!", HttpStatus.NOT_FOUND));

        if (!shop.getOwner().getPhone().equals(vendorPhone)) {
            log.warn("Cảnh báo bảo mật: User {} cố tình truy cập shop {}", vendorPhone, shopId);
            throw new AppException("Bạn không có quyền thao tác trên cửa hàng này!", HttpStatus.FORBIDDEN);
        }
        return shop;
    }

    private Category validateCategoryBelongsToShop(UUID categoryId, UUID shopId) {
        return categoryRepository.findByIdAndShopId(categoryId, shopId)
                .orElseThrow(() -> new AppException("Danh mục không hợp lệ hoặc không thuộc về cửa hàng này!", HttpStatus.BAD_REQUEST));
    }

    private FoodResponse mapToResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice())
                .imageUrl(food.getImageUrl())
                .isAvailable(food.getIsAvailable())
                .categoryId(food.getCategory().getId())
                .categoryName(food.getCategory().getName())
                .tags(food.getTags())
                .cuisine(food.getCuisine())
                .spicyLevel(food.getSpicyLevel())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FoodResponse> getAllFoods(UUID shopId, UUID categoryId, String vendorPhone, Pageable pageable) {
        validateShopOwnership(shopId, vendorPhone);

        if (categoryId != null) {
            validateCategoryBelongsToShop(categoryId, shopId);
        }

        return foodRepository.findByShopIdAndOptionalCategory(shopId, categoryId, pageable)
                .map(this::mapToResponse);
    }
    public Page<FoodExploreResponse> getExploreFoods(Pageable pageable){
        return foodRepository.exploreFoods(java.time.LocalTime.now(), pageable);
    }

    @Override
    @Transactional
    public FoodResponse createFood(UUID shopId, FoodRequest request, String vendorPhone) {
        Shop shop = validateShopOwnership(shopId, vendorPhone);

        Category category = validateCategoryBelongsToShop(request.getCategoryId(), shopId);

        if (foodRepository.existsByNameAndShopId(request.getName(), shopId)) {
            throw new AppException("Tên món ăn này đã tồn tại trong quán!", HttpStatus.BAD_REQUEST);
        }

        Food food = Food.builder()
                .shop(shop)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .tags(request.getTags())
                .cuisine(request.getCuisine())
                .spicyLevel(request.getSpicyLevel() != null ? request.getSpicyLevel() : 0)
                .build();

        Food savedFood = foodRepository.save(food);

        if ((savedFood.getTags() == null || savedFood.getTags().isEmpty()) 
                && (savedFood.getCuisine() == null || savedFood.getCuisine().isBlank())) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        geminiService.analyzeFoodAsync(savedFood.getId());
                    }
                });
            } else {
                geminiService.analyzeFoodAsync(savedFood.getId());
            }
        }

        return mapToResponse(savedFood);
    }

    @Override
    @Transactional
    public FoodResponse updateFood(UUID shopId, UUID foodId, FoodRequest request, String vendorPhone) {
        validateShopOwnership(shopId, vendorPhone);

        Food food = foodRepository.findByIdAndShopId(foodId, shopId)
                .orElseThrow(() -> new AppException("Món ăn không tồn tại!", HttpStatus.NOT_FOUND));

        if (!food.getCategory().getId().equals(request.getCategoryId())) {
            Category newCategory = validateCategoryBelongsToShop(request.getCategoryId(), shopId);
            food.setCategory(newCategory);
        }

        if (!food.getName().equals(request.getName()) &&
                foodRepository.existsByNameAndShopId(request.getName(), shopId)) {
            throw new AppException("Tên món ăn này đã tồn tại trong quán!", HttpStatus.BAD_REQUEST);
        }

        food.setName(request.getName());
        food.setDescription(request.getDescription());
        food.setPrice(request.getPrice());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            food.setImageUrl(request.getImageUrl());
        }

        if (request.getTags() != null) {
            food.setTags(request.getTags());
        }
        if (request.getCuisine() != null) {
            food.setCuisine(request.getCuisine());
        }
        if (request.getSpicyLevel() != null) {
            food.setSpicyLevel(request.getSpicyLevel());
        }

        Food savedFood = foodRepository.save(food);

        if ((savedFood.getTags() == null || savedFood.getTags().isEmpty()) 
                && (savedFood.getCuisine() == null || savedFood.getCuisine().isBlank())) {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        geminiService.analyzeFoodAsync(savedFood.getId());
                    }
                });
            } else {
                geminiService.analyzeFoodAsync(savedFood.getId());
            }
        }

        return mapToResponse(savedFood);
    }

    @Override
    @Transactional
    public void deleteFood(UUID shopId, UUID foodId, String vendorPhone) {
        validateShopOwnership(shopId, vendorPhone);

        Food food = foodRepository.findByIdAndShopId(foodId, shopId)
                .orElseThrow(() -> new AppException("Món ăn không tồn tại!", HttpStatus.NOT_FOUND));

        foodRepository.delete(food);
    }

    @Override
    @Transactional
    public FoodResponse toggleFoodAvailability(UUID shopId, UUID foodId, String vendorPhone) {
        validateShopOwnership(shopId, vendorPhone);

        Food food = foodRepository.findByIdAndShopId(foodId, shopId)
                .orElseThrow(() -> new AppException("Món ăn không tồn tại!", HttpStatus.NOT_FOUND));

        food.setIsAvailable(!food.getIsAvailable());

        return mapToResponse(foodRepository.save(food));

}}
