package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @PostMapping("/checkout")
    public ResponseEntity<List<Order>> checkout(@RequestBody CheckoutRequest request, Principal principal) {
        String phone = principal.getName();

        List<Order> orders = orderService.createOrder(phone, request);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Order>> getActiveOrders(Principal principal) {
        String phone = principal.getName();
        List<Order> activeOrders = orderService.getActiveOrders(phone);
        return ResponseEntity.ok(activeOrders);
    }
}
