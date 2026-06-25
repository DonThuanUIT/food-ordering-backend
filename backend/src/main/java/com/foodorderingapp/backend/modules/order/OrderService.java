package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Review;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardDto;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public interface OrderService {
    // Trong file OrderService.java
    OrderResponse createOrder(String phone, CheckoutRequest request);
    List<OrderResponse> getActiveOrders(String phone);
    List<OrderResponse> getOrderHistory(String phone);
    void submitOrderReview(UUID orderId, com.foodorderingapp.backend.modules.order.dto.request.ReviewSubmitRequest request, String phone);
    List<OrderResponse> getVendorOrders(UUID shopId, String statusName);
    OrderResponse updateOrderStatus(UUID orderId, UpdateStatusRequest request);
    VendorDashboardDto getVendorDashboard(UUID shopId, LocalDateTime startDate, LocalDateTime endDate);
}
