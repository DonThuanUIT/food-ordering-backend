package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.dto.request.ReviewRequest;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Review;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface OrderService {
    List<Order> createOrder(String phone, CheckoutRequest request);
    List<Order> getActiveOrders(String phone);
    List<Order> getOrderHistory(String phone);
    Review createReview(UUID orderId, ReviewRequest request, String phone);
}
