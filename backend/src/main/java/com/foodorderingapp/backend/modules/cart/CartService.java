package com.foodorderingapp.backend.modules.cart;

import com.foodorderingapp.backend.modules.cart.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {
    void addToCart(UUID foodId, Integer quantity, String note, String phone);
    CartResponse getCart(String phone);
    void updateCartItemQuantity (UUID cartItemId, Integer quantity, String phone);
    void deleteCartItem(UUID cartItemId, String phone);
    void clearShopCart(UUID shopId, String phone);
}
