package com.foodorderingapp.backend.modules.order.controller;

import com.foodorderingapp.backend.entity.Review;
import com.foodorderingapp.backend.modules.order.OrderService;
import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request, Principal principal) {
        String phone = principal.getName();
        // Đã sửa đổi: Trả về trực tiếp 1 OrderResponse thay vì List<OrderResponse>
        OrderResponse order = orderService.createOrder(phone, request);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/active")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(Principal principal) {
        String phone = principal.getName();
        List<OrderResponse> activeOrders = orderService.getActiveOrders(phone);
        return ResponseEntity.ok(activeOrders);
    }

    @GetMapping("/history")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(Principal principal) {
        String phone = principal.getName();
        List<OrderResponse> history = orderService.getOrderHistory(phone);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{orderId}/reviews")
    public ResponseEntity<Review> createReview(
            @PathVariable UUID orderId,
            @RequestBody ReviewRequest request,
            Principal principal) {
        Review review = orderService.createReview(orderId, request, principal.getName());
        return new ResponseEntity<>(review, HttpStatus.CREATED);
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<OrderResponse>> getVendorOrders(
            @PathVariable UUID shopId,
            @RequestParam(required = false) String status) {
        // API để chủ quán lấy danh sách đơn hàng (có thể lọc theo trạng thái)
        List<OrderResponse> orders = orderService.getVendorOrders(shopId, status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestBody UpdateStatusRequest request) {
        // API để chủ quán cập nhật trạng thái đơn hàng (Ví dụ: Từ PENDING sang PREPARING)
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{shopId}/dashboard")
    public ResponseEntity<VendorDashboardDto> getDashboardStats(
            @PathVariable UUID shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        // API thống kê doanh thu cho chủ quán
        VendorDashboardDto data = orderService.getVendorDashboard(shopId, startDate, endDate);
        return ResponseEntity.ok(data);
    }
}