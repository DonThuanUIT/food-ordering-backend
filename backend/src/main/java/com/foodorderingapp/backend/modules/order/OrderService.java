package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Review;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface OrderService {
    List<OrderResponse> createOrder(String phone, CheckoutRequest request);
    List<OrderResponse> getActiveOrders(String phone);
    List<OrderResponse> getOrderHistory(String phone);
    Review createReview(UUID orderId, ReviewRequest request, String phone);
    List<OrderResponse> getVendorOrders(UUID shopId, String statusName);
    OrderResponse updateOrderStatus(UUID orderId, UpdateStatusRequest request);

}
