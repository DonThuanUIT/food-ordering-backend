package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.CategoryRequest;
import com.foodorderingapp.backend.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<CategoryResponse> getAllCategories(UUID shopId, String vendorPhone);
    CategoryResponse createCategory(UUID shopId, CategoryRequest request, String vendorPhone);
    CategoryResponse updateCategory(UUID shopId, UUID categoryId, CategoryRequest request, String vendorPhone);
    void deleteCategory(UUID shopId, UUID categoryId, String vendorPhone);
}
