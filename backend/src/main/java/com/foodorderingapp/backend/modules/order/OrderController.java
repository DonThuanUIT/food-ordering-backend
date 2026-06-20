package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Review;
import com.foodorderingapp.backend.modules.order.OrderService;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardDto;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @PostMapping("/checkout")
    public ResponseEntity<List<OrderResponse>> checkout(@RequestBody CheckoutRequest request, Principal principal) {
        String phone = principal.getName();

        List<OrderResponse> orders = orderService.createOrder(phone, request);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(Principal principal) {
        String phone = principal.getName();
        List<OrderResponse> activeOrders = orderService.getActiveOrders(phone);
        return ResponseEntity.ok(activeOrders);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponse>> getOrderHistory (Principal principal){
        List<OrderResponse> history = orderService.getOrderHistory(principal.getName());

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
    @GetMapping("/{shopId}/dashboard")
    public ResponseEntity<VendorDashboardDto> getDashboardStats(
            @PathVariable UUID shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        VendorDashboardDto data = orderService.getVendorDashboard(shopId, startDate, endDate);
        return ResponseEntity.ok(data);
    }
}
