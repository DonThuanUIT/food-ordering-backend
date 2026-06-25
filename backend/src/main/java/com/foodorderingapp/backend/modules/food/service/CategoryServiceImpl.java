package com.foodorderingapp.backend.modules.food.service;

import com.foodorderingapp.backend.modules.food.dto.request.CategoryRequest;
import com.foodorderingapp.backend.modules.food.dto.response.CategoryResponse;
import com.foodorderingapp.backend.entity.Category;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.food.repository.CategoryRepository;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
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
    private final FoodRepository foodRepository;
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

    private String extractTextName(String name) {
        if (name == null) return "";
        int index = name.indexOf('|');
        if (index != -1 && index < name.length() - 1) {
            return name.substring(index + 1).trim().toLowerCase();
        }
        return name.trim().toLowerCase();
    }

    private String extractEmoji(String name) {
        if (name == null) return "";
        int index = name.indexOf('|');
        if (index != -1) {
            return name.substring(0, index).trim();
        }
        return "";
    }

    private boolean isGenericEmoji(String emoji) {
        return "🍽️".equals(emoji);
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

        String newTextName = extractTextName(request.getName());
        String newEmoji = extractEmoji(request.getName());
        List<Category> existingCategories = categoryRepository.findAllByShopId(shopId);

        boolean exists = existingCategories.stream()
                .anyMatch(c -> extractTextName(c.getName()).equals(newTextName));
        if (exists) {
            throw new AppException("Tên danh mục này đã tồn tại trong cửa hàng!", HttpStatus.BAD_REQUEST);
        }

        if (isGenericEmoji(newEmoji)) {
            boolean genericExists = existingCategories.stream()
                    .anyMatch(c -> isGenericEmoji(extractEmoji(c.getName())));
            if (genericExists) {
                throw new AppException("Đã tồn tại danh mục Khác rồi!", HttpStatus.BAD_REQUEST);
            } else {
                if (!newTextName.equals("khác") && !newTextName.equals("other")) {
                    throw new AppException("Bạn nên tạo danh mục này là 'Khác' vì món ăn này không có biểu tượng cụ thể.", HttpStatus.BAD_REQUEST);
                }
            }
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

        String newTextName = extractTextName(request.getName());
        String newEmoji = extractEmoji(request.getName());
        List<Category> existingCategories = categoryRepository.findAllByShopId(shopId);

        boolean nameExists = existingCategories.stream()
                .filter(c -> !c.getId().equals(categoryId))
                .anyMatch(c -> extractTextName(c.getName()).equals(newTextName));
        if (nameExists) {
            throw new AppException("Tên danh mục này đã tồn tại trong cửa hàng!", HttpStatus.BAD_REQUEST);
        }

        if (isGenericEmoji(newEmoji)) {
            boolean genericExists = existingCategories.stream()
                    .filter(c -> !c.getId().equals(categoryId))
                    .anyMatch(c -> isGenericEmoji(extractEmoji(c.getName())));
            if (genericExists) {
                throw new AppException("Đã tồn tại danh mục Khác rồi!", HttpStatus.BAD_REQUEST);
            } else {
                if (!newTextName.equals("khác") && !newTextName.equals("other")) {
                    throw new AppException("Bạn nên tạo danh mục này là 'Khác' vì món ăn này không có biểu tượng cụ thể.", HttpStatus.BAD_REQUEST);
                }
            }
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

        boolean hasFood = foodRepository.existsByCategoryId(categoryId);
        if (hasFood) {
            throw new AppException("Cannot delete this category because it contains food items!", HttpStatus.BAD_REQUEST);
        }
        categoryRepository.delete(category);
    }
}
