package com.foodorderingapp.backend.service.impl;

import com.foodorderingapp.backend.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.dto.response.CartItemResponse;
import com.foodorderingapp.backend.entity.*;
import com.foodorderingapp.backend.entity.enums.OrderStatus;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.CartItemRepository;
import com.foodorderingapp.backend.repository.OrderDetailRepository;
import com.foodorderingapp.backend.repository.OrderRepository;
import com.foodorderingapp.backend.repository.UserRepository;
import com.foodorderingapp.backend.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public List<Order> createOrder(String phone, CheckoutRequest request) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));
        List<CartItem> cartItems = cartItemRepository.findAllByUserPhone(phone);
        if (cartItems.isEmpty()) {
            throw new AppException("Gio hang cua ban dang trong", HttpStatus.BAD_REQUEST);

        }
        Map<Shop, List<CartItem>> itemsByShop = cartItems.stream().collect(Collectors.groupingBy(item -> item.getFood().getShop()));
        List<Order> savedOrders = new ArrayList<>();
        for (Map.Entry<Shop, List<CartItem>> entry : itemsByShop.entrySet()) {
            Shop shop = entry.getKey();
            List<CartItem> shopItems = entry.getValue();
            BigDecimal totalForShop = shopItems.stream()
                    .map(item -> item.getFood().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Order order = Order.builder()
                    .user(user)
                    .shop(shop)
                    .totalPrice(totalForShop)
                    .status(OrderStatus.PENDING)
                    .buildingSnapshot(request.getBuildingName())
                    .dropOffSnapshot(request.getDropOffPoint())
                    .build();
            List<OrderDetail> details = shopItems.stream().map(item ->
                    OrderDetail.builder()
                            .order(order)
                            .foodNameSnapshot(item.getFood().getName())
                            .priceSnapshot(item.getFood().getPrice())
                            .quantity(item.getQuantity())
                            .build()
            ).collect(Collectors.toList());
            order.setOrderDetails(details);
            savedOrders.add(orderRepository.save(order));

        }
        cartItemRepository.deleteAll(cartItems);

        return savedOrders;
    }

}
