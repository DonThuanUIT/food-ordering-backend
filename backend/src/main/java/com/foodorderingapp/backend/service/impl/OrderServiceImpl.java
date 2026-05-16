package com.foodorderingapp.backend.service.impl;

import com.foodorderingapp.backend.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.dto.request.ReviewRequest;
import com.foodorderingapp.backend.dto.response.CartItemResponse;
import com.foodorderingapp.backend.entity.*;
import com.foodorderingapp.backend.entity.enums.OrderStatus;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.*;
import com.foodorderingapp.backend.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.api.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
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
    @Override
    public List<Order> getActiveOrders(String phone) {
        return orderRepository.findActiveOrdersByPhone(phone);
    }

    @Override
    public List<Order> getOrderHistory(String phone){
        return orderRepository.findOrderHistoryByPhone(phone);
    }
    @Override
    @Transactional
    public Review createReview (UUID orderId, ReviewRequest request, String phone){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));
        if (!order.getUser().getPhone().equals(phone)) {
            throw new AppException("Bạn không có quyền đánh giá đơn hàng này", HttpStatus.FORBIDDEN);
        }
        if (!"COMPLETED".equalsIgnoreCase(order.getStatus().toString())) {
            throw new AppException("Chỉ đơn hàng đã hoàn thành mới có thể đánh giá", HttpStatus.BAD_REQUEST);
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new AppException("Đơn hàng này đã được đánh giá rồi", HttpStatus.CONFLICT);
        }
        Review review = Review.builder()
                .order(order)
                .user(order.getUser())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);

    }


}
