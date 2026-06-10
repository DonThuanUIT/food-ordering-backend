package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    void addToCart(UUID foodId, Integer quantity, String note, String phone);
    CartResponse getCart(String phone);
    void updateCartItemQuantity (UUID cartItemId, Integer quantity, String phone);
    void deleteCartItem(UUID cartItemId, String phone);
}
