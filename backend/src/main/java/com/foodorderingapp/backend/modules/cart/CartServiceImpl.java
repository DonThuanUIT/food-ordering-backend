package com.foodorderingapp.backend.modules.cart;

import com.foodorderingapp.backend.modules.cart.dto.response.CartItemResponse;
import com.foodorderingapp.backend.modules.cart.dto.response.CartResponse;
import com.foodorderingapp.backend.modules.cart.dto.response.ShopCartResponse;
import com.foodorderingapp.backend.entity.*;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.cart.repository.CartItemRepository;
import com.foodorderingapp.backend.modules.cart.repository.CartRepository;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.cart.CartService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;
    @Override
    @Transactional
    public void addToCart(UUID foodId, Integer quantity, String note, String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));

        Cart cart = cartRepository.findByUserPhone(phone)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new AppException("Món ăn không tồn tại", HttpStatus.NOT_FOUND));

        if (!isOrderableFood(food)) {
            throw new AppException("Quán ăn hiện không khả dụng", HttpStatus.BAD_REQUEST);
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndFoodId(cart.getId(), foodId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setNote(note);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .food(food)
                    .quantity(quantity)
                    .note(note)
                    .build();
            cartItemRepository.save(newItem);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String phone) {
        List<CartItem> items = cartItemRepository.findAllByUserPhone(phone);
        items = items.stream()
                .filter(item -> item != null && isOrderableFood(item.getFood()))
                .toList();

        if (items.isEmpty()) {
            return new CartResponse(List.of(), BigDecimal.ZERO);
        }
        Map<Shop, List<CartItem>> groupedByShop = items.stream()
                .collect(Collectors.groupingBy(item -> item.getFood().getShop()));

        List<ShopCartResponse> shops = groupedByShop.entrySet().stream()
                .map(entry -> new ShopCartResponse(
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getValue().stream().map(this::mapToItemResponse).toList()
                )).toList();

        BigDecimal totalAmount = items.stream()
                .map(item -> item.getFood().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))) // price * quantity
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(shops, totalAmount);
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getFood().getId(),
                item.getFood().getName(),
                item.getFood().getImageUrl(),
                item.getFood().getPrice(),
                item.getQuantity(),
                item.getNote()
        );
    }

    private boolean isOrderableFood(Food food) {
        if (food == null || food.getShop() == null) {
            return false;
        }
        Shop shop = food.getShop();
        return shop.getStatus() == ShopStatus.APPROVED
                && Boolean.TRUE.equals(shop.getIsActive())
                && !isShopOwnerLocked(shop)
                && Boolean.TRUE.equals(food.getIsAvailable());
    }

    private boolean isShopOwnerLocked(Shop shop) {
        return shop.getOwner() != null && Boolean.TRUE.equals(shop.getOwner().getIsLocked());
    }

    @Override
    @Transactional
    public void updateCartItemQuantity(UUID cartItemId, Integer quantity, String phone) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(()-> new AppException("Không tìm thấy món ăn trong giỏ hàng", HttpStatus.NOT_FOUND));
        if(!cartItem.getCart().getUser().getPhone().equals(phone)) {
            throw new AppException("Bạn không có quyền chỉnh sửa giỏ hàng này", HttpStatus.FORBIDDEN);
        }
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }
    @Override
    @Transactional
    public void deleteCartItem(UUID cartItemId, String phone) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException("Không tìm thấy món ăn trong giỏ hàng", HttpStatus.NOT_FOUND));
        if(!cartItem.getCart().getUser().getPhone().equals(phone)) {
            throw new AppException("Ban khong co quyen xoa muc gio hang nay", HttpStatus.FORBIDDEN);
        }
        cartItemRepository.delete(cartItem);
    }
    @Override
    @Transactional
    public void clearShopCart(UUID shopId, String phone) {
        cartItemRepository.deleteAllByShopIdAndUserPhone(shopId, phone);
    }
}
