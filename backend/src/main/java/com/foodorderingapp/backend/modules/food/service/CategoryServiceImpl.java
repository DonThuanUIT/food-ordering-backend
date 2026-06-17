package com.foodorderingapp.backend.modules.food.service;

import com.foodorderingapp.backend.modules.food.dto.request.CategoryRequest;
import com.foodorderingapp.backend.modules.food.dto.response.CategoryResponse;
import com.foodorderingapp.backend.entity.Category;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.food.repository.CategoryRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import com.foodorderingapp.backend.modules.food.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;

    private Shop validateShopOwnership(UUID shopId, String vendorPhone) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("The store doesn't exist!", HttpStatus.NOT_FOUND));

        if (!shop.getOwner().getPhone().equals(vendorPhone)) {
            log.warn("Security warning: User {} intentionally accessed shop {}", vendorPhone, shopId);
            throw new AppException("You do not have permission to operate this store!", HttpStatus.FORBIDDEN);
        }
        return shop;
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(UUID shopId, String vendorPhone) {
        validateShopOwnership(shopId, vendorPhone);
        return categoryRepository.findAllByShopId(shopId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(UUID shopId, CategoryRequest request, String vendorPhone) {
        Shop shop = validateShopOwnership(shopId, vendorPhone);

        if (categoryRepository.existsByNameAndShopId(request.getName(), shopId)) {
            throw new AppException("This category name already exists in the store!", HttpStatus.BAD_REQUEST);
        }

        Category category = Category.builder()
                .name(request.getName())
                .shop(shop)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID shopId, UUID categoryId, CategoryRequest request, String vendorPhone) {
        validateShopOwnership(shopId, vendorPhone);

        Category category = categoryRepository.findByIdAndShopId(categoryId, shopId)
                .orElseThrow(() -> new AppException("Category does not exist in this store!", HttpStatus.NOT_FOUND));

        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByNameAndShopId(request.getName(), shopId)) {
            throw new AppException("This category name already exists in the store!", HttpStatus.BAD_REQUEST);
        }

        category.setName(request.getName());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID shopId, UUID categoryId, String vendorPhone) {
        validateShopOwnership(shopId, vendorPhone);

        Category category = categoryRepository.findByIdAndShopId(categoryId, shopId)
                .orElseThrow(() -> new AppException("Category does not exist in this store!", HttpStatus.NOT_FOUND));

        // TODO: Mở rộng sau này -> Check xem Category có đang chứa FoodItem nào không trước khi xóa cứng.
        // Tạm thời cho phép xóa cứng.
        categoryRepository.delete(category);
    }
}
