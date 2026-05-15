package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.dto.request.ReviewRequest;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Review;
import com.foodorderingapp.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

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

    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrderHistory (Principal principal){
        List<Order> history = orderService.getOrderHistory(principal.getName());

        return ResponseEntity.ok(history);
    }
    @PostMapping("/{orderId}/reviews")
    public ResponseEntity<Review> createReview(
            @PathVariable UUID orderId,
            @RequestBody ReviewRequest request,
            Principal principal
    ) {
        Review review = orderService.createReview(orderId, request, principal.getName());
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }
}
