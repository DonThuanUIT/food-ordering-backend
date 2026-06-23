package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.cart.dto.response.CartItemResponse;
import com.foodorderingapp.backend.modules.order.dto.response.*;
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
import com.foodorderingapp.backend.modules.voucher.repository.VoucherRepository;
import com.foodorderingapp.backend.entity.Voucher;
import com.foodorderingapp.backend.entity.Food;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final VoucherRepository voucherRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
                .displayStatus(calculateOrderDisplayStatus(order.getStatus()))
                .building(order.getBuildingSnapshot())
                .dropOff(order.getDropOffSnapshot())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .details(details)
                .voucherCode(order.getVoucherCode())
                .discountAmount(order.getDiscountAmount())
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

            BigDecimal discountAmount = BigDecimal.ZERO;
            String appliedVoucherCode = null;

            if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
                java.util.Optional<Voucher> voucherOpt = voucherRepository.findByShopIdAndCode(shop.getId(), request.getVoucherCode().trim().toUpperCase());
                if (voucherOpt.isPresent()) {
                    Voucher voucher = voucherOpt.get();
                    java.time.LocalDateTime now = java.time.LocalDateTime.now();
                    boolean isValid = Boolean.TRUE.equals(voucher.getIsActive())
                            && (voucher.getStartDate() == null || voucher.getStartDate().isBefore(now))
                            && (voucher.getEndDate() == null || voucher.getEndDate().isAfter(now));

                    if (isValid) {
                        BigDecimal applicableTotal = BigDecimal.ZERO;
                        if ("ALL_MENU".equals(voucher.getApplyType())) {
                            applicableTotal = totalForShop;
                        } else if ("SPECIFIC_FOODS".equals(voucher.getApplyType()) && voucher.getFoods() != null) {
                            java.util.Set<UUID> applicableFoodIds = voucher.getFoods().stream().map(Food::getId).collect(Collectors.toSet());
                            for (CartItem item : shopItems) {
                                if (applicableFoodIds.contains(item.getFood().getId())) {
                                    BigDecimal itemTotal = item.getFood().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                                    applicableTotal = applicableTotal.add(itemTotal);
                                }
                            }
                        }

                        if (applicableTotal.compareTo(voucher.getMinOrderValue()) >= 0 && applicableTotal.compareTo(BigDecimal.ZERO) > 0) {
                            if ("FIXED_AMOUNT".equals(voucher.getDiscountType())) {
                                discountAmount = voucher.getDiscountValue();
                                if (discountAmount.compareTo(totalForShop) > 0) {
                                    discountAmount = totalForShop;
                                }
                            } else if ("PERCENTAGE".equals(voucher.getDiscountType())) {
                                discountAmount = applicableTotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                                if (voucher.getMaxDiscountValue() != null && discountAmount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                                    discountAmount = voucher.getMaxDiscountValue();
                                }
                                if (discountAmount.compareTo(totalForShop) > 0) {
                                    discountAmount = totalForShop;
                                }
                            }
                            appliedVoucherCode = voucher.getCode();
                        }
                    }
                }
            }

            Order order = Order.builder()
                    .user(user)
                    .shop(shop)
                    .totalPrice(totalForShop.subtract(discountAmount))
                    .status(OrderStatus.PENDING)
                    .buildingSnapshot(buildingName)
                    .dropOffSnapshot(dropOffPoint)
                    .voucherCode(appliedVoucherCode)
                    .discountAmount(discountAmount)
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

        List<OrderResponse> responses = savedOrders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
        for (OrderResponse res : responses) {
            try {
                java.util.Optional<Order> orderOpt = savedOrders.stream().filter(o -> o.getId().equals(res.getId())).findFirst();
                if (orderOpt.isPresent()) {
                    String destination = "/topic/shop/" + orderOpt.get().getShop().getId() + "/orders";
                    messagingTemplate.convertAndSend(destination, res);
                }
            } catch (Exception e) {
                // Ignore to avoid blocking checkout if WebSocket fails
            }
        }

        return responses;
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
        OrderResponse response = mapToOrderResponse(orderRepository.save(order));

        try {
            // 1. Notify vendor (for dashboard/orders list update)
            String shopDestination = "/topic/shop/" + order.getShop().getId() + "/orders";
            messagingTemplate.convertAndSend(shopDestination, response);

            // 2. Notify customer (for order details/status update)
            String customerDestination = "/topic/orders/customer/" + order.getUser().getPhone();
            messagingTemplate.convertAndSend(customerDestination, response);
        } catch (Exception e) {
            // Ignore
        }

        return response;
    }
    @Override
    public VendorDashboardDto getVendorDashboard(UUID shopId, LocalDateTime startDate, LocalDateTime endDate) {
        // 1. Khởi tạo ngày mặc định nếu client không truyền lên (30 ngày gần đây)
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();

        // 2. Lấy dữ liệu tổng quan thô từ câu Query Số 1 (Bảng tổng hợp doanh thu, tổng số đơn)
        VendorDashboardResponse overview = orderRepository.getVendorDashboardStats(shopId, startDate, endDate);

        // Tính chu kỳ trước có cùng số ngày
        java.time.Duration duration = java.time.Duration.between(startDate, endDate);
        LocalDateTime previousStartDate = startDate.minus(duration);
        LocalDateTime previousEndDate = startDate;
        VendorDashboardResponse previousOverview = orderRepository.getVendorDashboardStats(shopId, previousStartDate, previousEndDate);

        BigDecimal currentRevenue = overview != null && overview.getTotalRevenue() != null ? overview.getTotalRevenue() : java.math.BigDecimal.ZERO;
        Long currentOrders = overview != null && overview.getTotalOrders() != null ? overview.getTotalOrders() : 0L;
        Double currentCompletionRate = overview != null && overview.getCompletionRate() != null ? overview.getCompletionRate() : 0.0;
        BigDecimal currentAov = overview != null && overview.getAverageOrderValue() != null ? overview.getAverageOrderValue() : java.math.BigDecimal.ZERO;

        BigDecimal prevRevenue = previousOverview != null && previousOverview.getTotalRevenue() != null ? previousOverview.getTotalRevenue() : java.math.BigDecimal.ZERO;
        Long prevOrders = previousOverview != null && previousOverview.getTotalOrders() != null ? previousOverview.getTotalOrders() : 0L;
        Double prevCompletionRate = previousOverview != null && previousOverview.getCompletionRate() != null ? previousOverview.getCompletionRate() : 0.0;
        BigDecimal prevAov = previousOverview != null && previousOverview.getAverageOrderValue() != null ? previousOverview.getAverageOrderValue() : java.math.BigDecimal.ZERO;

        Double revenueGrowth = calculateGrowthRate(currentRevenue, prevRevenue);
        Double orderCountGrowth = calculateGrowthRate(currentOrders, prevOrders);
        Double completionRateGrowth = calculateGrowthRate(currentCompletionRate, prevCompletionRate);
        Double averageOrderValueGrowth = calculateGrowthRate(currentAov, prevAov);

        // 3. Xử lý yêu cầu 2: Chuyển đổi dữ liệu List<Map> thành List<TrendData>
        List<TrendData> trends = orderRepository.getOrderTrends(shopId, startDate, endDate).stream()
                .map(row -> TrendData.builder()
                        .date(java.time.LocalDate.parse(row.get("date").toString()))
                        .revenue(((Number) row.get("revenue")).longValue())
                        .orderCount(((Number) row.get("order_count")).longValue())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // 4. Xử lý yêu cầu 3: Chuyển đổi dữ liệu Top sản phẩm thành List<TopProductData>
        List<TopProductData> topProducts = orderRepository.getTopSellingFoods(shopId, startDate, endDate).stream()
                .map(row -> TopProductData.builder()
                        .foodId(null) // Đặt null vì thiết kế database dùng snapshot không lưu ID món ăn gốc
                        .foodName(row.get("food_name") != null ? row.get("food_name").toString() : "Món ăn ẩn")
                        .quantitySold(row.get("quantity_sold") != null ? ((Number) row.get("quantity_sold")).longValue() : 0L)
                        .revenue(row.get("revenue") != null ? ((Number) row.get("revenue")).longValue() : 0L)
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // 5. Xử lý yêu cầu 4: Đếm chi tiết số đơn của TẤT CẢ các trạng thái phục vụ Pie Chart
        java.util.Map<String, Long> statusBreakdown = new java.util.HashMap<>();
        // Tạo giá trị mặc định để tránh hiển thị thiếu trên UI Frontend
        statusBreakdown.put("PENDING", 0L);
        statusBreakdown.put("PREPARING", 0L);
        statusBreakdown.put("DELIVERING", 0L);
        statusBreakdown.put("COMPLETED", 0L);
        statusBreakdown.put("CANCELLED", 0L);

        List<Map<String, Object>> dbStatus = orderRepository.getOrderStatusBreakdown(shopId, startDate, endDate);
        if (dbStatus != null) {
            for (Map<String, Object> row : dbStatus) {
                if (row.get("status") != null && row.get("count") != null) {
                    statusBreakdown.put(row.get("status").toString(), ((Number) row.get("count")).longValue());
                }
            }
        }

        // 6. Ráp nối tất cả nguyên liệu đã chế biến vào VendorDashboardDto để trả về cho Controller
        return VendorDashboardDto.builder()
                .totalRevenue(currentRevenue)
                .totalOrders(currentOrders)
                .completionRate(currentCompletionRate)
                .averageOrderValue(currentAov)
                .revenueGrowth(revenueGrowth)
                .orderCountGrowth(orderCountGrowth)
                .completionRateGrowth(completionRateGrowth)
                .averageOrderValueGrowth(averageOrderValueGrowth)
                .orderTrends(trends)
                .topSellingProducts(topProducts)
                .orderStatusBreakdown(statusBreakdown)
                .build();
    }

    private Double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current == null || current.compareTo(BigDecimal.ZERO) == 0) {
                return 0.0;
            }
            return 100.0;
        }
        if (current == null) {
            current = BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Double calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            if (current == null || current == 0) {
                return 0.0;
            }
            return 100.0;
        }
        if (current == null) {
            current = 0L;
        }
        double growth = ((double) (current - previous) / previous) * 100.0;
        return BigDecimal.valueOf(growth).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private Double calculateGrowthRate(Double current, Double previous) {
        if (previous == null || previous == 0.0) {
            if (current == null || current == 0.0) {
                return 0.0;
            }
            return 100.0;
        }
        if (current == null) {
            current = 0.0;
        }
        double growth = ((current - previous) / previous) * 100.0;
        return BigDecimal.valueOf(growth).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    private String calculateOrderDisplayStatus(OrderStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "CHỜ XÁC NHẬN";
            case CONFIRMED -> "ĐÃ XÁC NHẬN";
            case DELIVERING -> "ĐANG GIAO HÀNG";
            case RECEIVED -> "ĐÃ NHẬN";
            case FAILED -> "THẤT BẠI";
            case REJECTED -> "BỊ TỪ CHỐI";
            case COMPLETED -> "ĐÃ HOÀN THÀNH";
            case CANCELLED -> "ĐÃ HỦY";
        };
    }
}