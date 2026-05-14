package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.entity.Order;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface OrderService {
    List<Order> createOrder(String phone, CheckoutRequest request);
}
