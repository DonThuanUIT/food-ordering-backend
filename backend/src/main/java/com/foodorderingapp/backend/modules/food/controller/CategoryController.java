package com.foodorderingapp.backend.modules.food.controller;

import com.foodorderingapp.backend.modules.food.dto.request.CategoryRequest;
import com.foodorderingapp.backend.modules.food.dto.response.CategoryResponse;
import com.foodorderingapp.backend.modules.food.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendor/shops/{shopId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @PathVariable UUID shopId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        List<CategoryResponse> responses = categoryService.getAllCategories(shopId, vendorPhone);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable UUID shopId,
            @Valid @RequestBody CategoryRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        CategoryResponse response = categoryService.createCategory(shopId, request, vendorPhone);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID shopId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        CategoryResponse response = categoryService.updateCategory(shopId, categoryId, request, vendorPhone);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID shopId,
            @PathVariable UUID categoryId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        categoryService.deleteCategory(shopId, categoryId, vendorPhone);
        return ResponseEntity.noContent().build();
    }
}
