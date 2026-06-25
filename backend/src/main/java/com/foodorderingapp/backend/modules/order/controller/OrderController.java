package com.foodorderingapp.backend.modules.order.controller;

import com.foodorderingapp.backend.entity.Review;
import com.foodorderingapp.backend.entity.ShopReview;
import com.foodorderingapp.backend.entity.FoodReview;
import com.foodorderingapp.backend.modules.order.OrderService;
import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewSubmitRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardDto;
import com.foodorderingapp.backend.modules.order.repository.ShopReviewRepository;
import com.foodorderingapp.backend.modules.order.repository.FoodReviewRepository;
import com.foodorderingapp.backend.modules.order.repository.ReviewRepository;
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
    private final ShopReviewRepository shopReviewRepository;
    private final FoodReviewRepository foodReviewRepository;
    private final ReviewRepository reviewRepository;


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
    public ResponseEntity<Void> submitReview(
            @PathVariable UUID orderId,
            @RequestBody ReviewSubmitRequest request,
            Principal principal) {
        orderService.submitOrderReview(orderId, request, principal.getName());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/shop/{shopId}/reviews")
    public ResponseEntity<List<ShopReview>> getShopReviews(@PathVariable UUID shopId) {
        return ResponseEntity.ok(shopReviewRepository.findByShopIdOrderByCreatedAtDesc(shopId));
    }

    @GetMapping("/shop/{shopId}/delivery-reviews")
    public ResponseEntity<List<Review>> getDeliveryReviews(@PathVariable UUID shopId) {
        return ResponseEntity.ok(reviewRepository.findByShopIdOrderByCreatedAtDesc(shopId));
    }

    @GetMapping("/shop/{shopId}/rating")
    public ResponseEntity<Double> getShopAverageRating(@PathVariable UUID shopId) {
        Double avg = shopReviewRepository.getAverageRatingForShop(shopId);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
    }

    @GetMapping("/food/{foodId}/reviews")
    public ResponseEntity<List<FoodReview>> getFoodReviews(@PathVariable UUID foodId) {
        return ResponseEntity.ok(foodReviewRepository.findByFoodIdOrderByCreatedAtDesc(foodId));
    }

    @GetMapping("/food/{foodId}/rating")
    public ResponseEntity<Double> getFoodAverageRating(@PathVariable UUID foodId) {
        Double avg = foodReviewRepository.getAverageRatingForFood(foodId);
        return ResponseEntity.ok(avg != null ? avg : 0.0);
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