package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.cart.dto.response.CartItemResponse;
import com.foodorderingapp.backend.modules.order.dto.response.OrderDetailResponse;
import com.foodorderingapp.backend.modules.order.dto.response.OrderResponse;
import com.foodorderingapp.backend.entity.*;
import com.foodorderingapp.backend.core.enums.OrderStatus;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.cart.repository.CartItemRepository;
import com.foodorderingapp.backend.modules.order.repository.OrderRepository;
import com.foodorderingapp.backend.modules.order.repository.OrderDetailRepository;
import com.foodorderingapp.backend.modules.order.repository.ReviewRepository;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.building.repository.BuildingRepository;
import com.foodorderingapp.backend.modules.building.repository.DropOffPointRepository;
import com.foodorderingapp.backend.modules.order.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final BuildingRepository buildingRepository;
    private final DropOffPointRepository dropOffPointRepository;

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderDetailResponse> details = order.getOrderDetails().stream()
                .map(d -> OrderDetailResponse.builder()
                        .foodName(d.getFoodNameSnapshot())
                        .price(d.getPriceSnapshot())
                        .quantity(d.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .shopName(order.getShop().getName())
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getUser().getPhone())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .building(order.getBuildingSnapshot())
                .dropOff(order.getDropOffSnapshot())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .details(details)
                .build();
    }
    @Transactional
    public List<OrderResponse> createOrder(String phone, CheckoutRequest request) {
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
            // Resolve building name and drop-off point - prefer IDs for clean data, fallback to raw strings
            String buildingName = request.getBuildingName();
            if (request.getBuildingId() != null) {
                buildingName = buildingRepository.findById(request.getBuildingId())
                        .map(Building::getName)
                        .orElse(buildingName);
            }

            String dropOffPoint = request.getDropOffPoint();
            if (request.getDropOffPointId() != null) {
                dropOffPoint = dropOffPointRepository.findById(request.getDropOffPointId())
                        .map(DropOffPoint::getName)
                        .orElse(dropOffPoint);
            }

            Order order = Order.builder()
                    .user(user)
                    .shop(shop)
                    .totalPrice(totalForShop)
                    .status(OrderStatus.PENDING)
                    .buildingSnapshot(buildingName)
                    .dropOffSnapshot(dropOffPoint)
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

        return savedOrders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }
    @Override
    public List<OrderResponse> getActiveOrders(String phone) {
        List<OrderStatus> activeStatuses = List.of(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.DELIVERING
        );
        return orderRepository.findActiveOrdersByPhone(phone, activeStatuses).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrderHistory(String phone){
        return orderRepository.findOrderHistoryByPhone(phone).stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public Review createReview (UUID orderId, ReviewRequest request, String phone){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));
        if (!order.getUser().getPhone().equals(phone)) {
            throw new AppException("Bạn không có quyền đánh giá đơn hàng này", HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
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
    @Override
    public List<OrderResponse> getVendorOrders(UUID shopId, String statusName) {
        OrderStatus status = null;
        if(statusName != null && !statusName.isEmpty()){
            try {
                status = OrderStatus.valueOf(statusName.toUpperCase());
            } catch (IllegalArgumentException e){
                throw new AppException("Trang thai don hang khong hop le", HttpStatus.BAD_REQUEST);
            }
        }
        return orderRepository.findByShopIdAndStatus(shopId, status)
                .stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, UpdateStatusRequest request){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Khong tim thay don hang", HttpStatus.NOT_FOUND));
        OrderStatus newStatus;
        OrderStatus currentStatus = order.getStatus();
        try {
            newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Trạng thái mới không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        if (currentStatus == OrderStatus.COMPLETED) {
            throw new AppException("Không thể thay đổi trạng thái của đơn hàng đã kết thúc", HttpStatus.BAD_REQUEST);
        }
        if (newStatus != OrderStatus.CANCELLED) {
            boolean isValidFlow = switch (currentStatus) {
                case PENDING -> newStatus == OrderStatus.CONFIRMED;
                case CONFIRMED -> newStatus == OrderStatus.DELIVERING;
                case DELIVERING -> newStatus == OrderStatus.COMPLETED;
                default -> false;
            };

            if (!isValidFlow) {
                throw new AppException("Sự thay đổi trạng thái từ " + currentStatus + " sang " + newStatus + " là không hợp lệ", HttpStatus.BAD_REQUEST);
            }
        }
        if (newStatus == OrderStatus.CANCELLED) {
            if (request.getCancelReason() == null || request.getCancelReason().isBlank()) {
                throw new AppException("Bắt buộc phải cung cấp lý do khi hủy đơn hàng", HttpStatus.BAD_REQUEST);
            }
            order.setCancelReason(request.getCancelReason());
        }
        order.setStatus(newStatus);
        return mapToOrderResponse(orderRepository.save(order));
    }


}