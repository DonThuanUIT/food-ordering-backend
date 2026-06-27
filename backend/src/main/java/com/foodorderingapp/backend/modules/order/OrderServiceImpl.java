package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.CheckoutRequest;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewRequest;
import com.foodorderingapp.backend.modules.order.dto.request.UpdateStatusRequest;
import com.foodorderingapp.backend.modules.cart.dto.response.CartItemResponse;
import com.foodorderingapp.backend.modules.order.dto.response.*;
import com.foodorderingapp.backend.entity.*;
import com.foodorderingapp.backend.core.enums.OrderStatus;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.core.enums.UserRole;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.cart.repository.CartItemRepository;
import com.foodorderingapp.backend.modules.order.repository.*;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.building.repository.BuildingRepository;
import com.foodorderingapp.backend.modules.order.OrderService;
import com.foodorderingapp.backend.modules.voucher.repository.VoucherRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewSubmitRequest;
import com.foodorderingapp.backend.entity.Voucher;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.entity.ShopReview;
import com.foodorderingapp.backend.entity.FoodReview;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final ShopReviewRepository shopReviewRepository;
    private final FoodReviewRepository foodReviewRepository;
    private final FoodRepository foodRepository;
    private final BuildingRepository buildingRepository;
    private final VoucherRepository voucherRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ShopRepository shopRepository;

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderDetailResponse> details = order.getOrderDetails().stream()
                .map(d -> OrderDetailResponse.builder()
                        .foodName(d.getFoodNameSnapshot())
                        .price(d.getPriceSnapshot())
                        .quantity(d.getQuantity())
                        .foodId(d.getFood() != null ? d.getFood().getId() : null)
                        .imageUrl(d.getFood() != null ? d.getFood().getImageUrl() : null)
                        .build())
                .collect(Collectors.toList());

        Double buildingLat = null;
        Double buildingLng = null;
        if (order.getBuildingSnapshot() != null) {
            java.util.Optional<Building> bOpt = buildingRepository.findByName(order.getBuildingSnapshot());
            if (bOpt.isPresent()) {
                buildingLat = bOpt.get().getLatitude();
                buildingLng = bOpt.get().getLongitude();
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .shopName(order.getShop().getName())
                .customerName(order.getUser().getFullName())
                .customerPhone(order.getUser().getPhone())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .displayStatus(calculateOrderDisplayStatus(order.getStatus()))
                .building(order.getBuildingSnapshot())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .details(details)
                .voucherCode(order.getVoucherCode())
                .discountAmount(order.getDiscountAmount())
                .isReviewed(reviewRepository.existsByOrderId(order.getId()))
                .shipperId(order.getShipper() != null ? order.getShipper().getId() : null)
                .shipperName(order.getShipper() != null ? order.getShipper().getFullName() : null)
                .shipperPhone(order.getShipper() != null ? order.getShipper().getPhone() : null)
                .shipperLatitude(order.getShipperLatitude())
                .shipperLongitude(order.getShipperLongitude())
                .shopId(order.getShop().getId())
                .shopAddress(order.getShop().getAddress())
                .shopLatitude(order.getShop().getLatitude())
                .shopLongitude(order.getShop().getLongitude())
                .buildingLatitude(buildingLat)
                .buildingLongitude(buildingLng)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse createOrder(String phone, CheckoutRequest request) {
        // 1. Xác thực User & Shop
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));

        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new AppException("Quán ăn không tồn tại", HttpStatus.NOT_FOUND));

        if (shop.getStatus() != ShopStatus.APPROVED
                || !Boolean.TRUE.equals(shop.getIsOpen())
                || !Boolean.TRUE.equals(shop.getIsActive())
                || isShopOwnerLocked(shop)) {
            throw new AppException("Quán ăn hiện đang đóng cửa hoặc ngừng hoạt động", HttpStatus.BAD_REQUEST);
        }

        // 2. Lấy dữ liệu Giỏ hàng & Validate Option 1A (Strict Shop Matching)
        if (!isShopOpenNow(shop)) {
            throw new AppException("Quán đã đóng cửa, vui lòng đặt hàng trong giờ mở cửa", HttpStatus.BAD_REQUEST);
        }

        List<CartItem> selectedCartItems = cartItemRepository.findAllById(request.getCartItemIds());

        if (selectedCartItems.isEmpty() || selectedCartItems.size() != request.getCartItemIds().size()) {
            throw new AppException("Một số món ăn không còn tồn tại trong giỏ hàng. Vui lòng tải lại!", HttpStatus.BAD_REQUEST);
        }

        BigDecimal totalOrderPrice = BigDecimal.ZERO;

        for (CartItem item : selectedCartItems) {
            if (!item.getFood().getShop().getId().equals(shop.getId())) {
                throw new AppException("Lỗi bảo mật: Món '" + item.getFood().getName() + "' không thuộc quán này!", HttpStatus.BAD_REQUEST);
            }
            if (!Boolean.TRUE.equals(item.getFood().getIsAvailable())) {
                throw new AppException("Món ăn '" + item.getFood().getName() + "' hiện đã hết hàng.", HttpStatus.BAD_REQUEST);
            }
            // Tính tổng tiền gốc
            BigDecimal itemTotal = item.getFood().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalOrderPrice = totalOrderPrice.add(itemTotal);
        }

        // 3. Xử lý logic Voucher cực mạnh (Option 2C)
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedVoucherCode = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().trim().isEmpty()) {
            Voucher voucher = voucherRepository.findByShopIdAndCode(shop.getId(), request.getVoucherCode().trim().toUpperCase())
                    .orElseThrow(() -> new AppException("Mã giảm giá không tồn tại", HttpStatus.BAD_REQUEST));

            LocalDateTime now = LocalDateTime.now();
            if (!Boolean.TRUE.equals(voucher.getIsActive()) ||
                    (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) ||
                    (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now))) {
                throw new AppException("Mã giảm giá đã hết hạn hoặc chưa được kích hoạt", HttpStatus.BAD_REQUEST);
            }

            BigDecimal applicableTotal = BigDecimal.ZERO;

            // Tính toán số tiền được phép áp dụng mã giảm giá
            if ("ALL_MENU".equals(voucher.getApplyType())) {
                applicableTotal = totalOrderPrice;
            } else if ("SPECIFIC_FOODS".equals(voucher.getApplyType()) && voucher.getFoods() != null) {
                java.util.Set<UUID> applicableFoodIds = voucher.getFoods().stream().map(Food::getId).collect(Collectors.toSet());
                for (CartItem item : selectedCartItems) {
                    if (applicableFoodIds.contains(item.getFood().getId())) {
                        applicableTotal = applicableTotal.add(item.getFood().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }
            }

            if (applicableTotal.compareTo(BigDecimal.ZERO) == 0) {
                throw new AppException("Mã giảm giá không áp dụng cho các món trong đơn hàng này", HttpStatus.BAD_REQUEST);
            }

            if (applicableTotal.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new AppException("Đơn hàng chưa đạt giá trị tối thiểu (" + voucher.getMinOrderValue() + ") để dùng mã này", HttpStatus.BAD_REQUEST);
            }

            // Tính số tiền được giảm
            if ("FIXED_AMOUNT".equals(voucher.getDiscountType())) {
                discountAmount = voucher.getDiscountValue();
            } else if ("PERCENTAGE".equals(voucher.getDiscountType())) {
                discountAmount = applicableTotal.multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                if (voucher.getMaxDiscountValue() != null && voucher.getMaxDiscountValue().compareTo(BigDecimal.ZERO) > 0) {
                    if (discountAmount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                        discountAmount = voucher.getMaxDiscountValue();
                    }
                }
            }

            // Chặn lỗi giảm giá lố tiền đơn hàng
            if (discountAmount.compareTo(totalOrderPrice) > 0) {
                discountAmount = totalOrderPrice;
            }
            appliedVoucherCode = voucher.getCode();
        }

        // 4. Phân giải địa chỉ giao hàng
        String buildingName = buildingRepository.findById(request.getBuildingId())
                .map(Building::getName)
                .orElseThrow(() -> new AppException("Tòa nhà không tồn tại", HttpStatus.BAD_REQUEST));

        // 5. Đơn mới chờ quán duyệt, thanh toán xử lý ngoài hệ thống.
        OrderStatus initialStatus = OrderStatus.PENDING;

        // 6. Khởi tạo Đơn hàng (Order)
        Order order = Order.builder()
                .user(user)
                .shop(shop)
                .totalPrice(totalOrderPrice.subtract(discountAmount))
                .status(initialStatus)
                .buildingSnapshot(buildingName)
                .voucherCode(appliedVoucherCode)
                .discountAmount(discountAmount)
                .build();

        // 7. Tạo Chi tiết Đơn hàng (OrderDetail) với cơ chế Snapshot
        List<OrderDetail> details = selectedCartItems.stream().map(item ->
                OrderDetail.builder()
                        .order(order)
                        .food(item.getFood())
                        .foodNameSnapshot(item.getFood().getName())
                        .priceSnapshot(item.getFood().getPrice())
                        .quantity(item.getQuantity())
                        .build()
        ).collect(Collectors.toList());

        order.setOrderDetails(details);

        // 8. Lưu DB & Dọn dẹp giỏ hàng
        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteAll(selectedCartItems);

        OrderResponse response = mapToOrderResponse(savedOrder);

        // 9. Bắn thông báo Real-time cho Quán ăn
        try {
            String destination = "/topic/shop/" + shop.getId() + "/orders";
            messagingTemplate.convertAndSend(destination, response);
        } catch (Exception e) {
            // Không block luồng đặt hàng nếu WebSocket sập
        }

        return response;
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
    public OrderResponse cancelPendingOrder(UUID orderId, String studentPhone, String cancelReason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));

        if (order.getUser() == null || !order.getUser().getPhone().equals(studentPhone)) {
            throw new AppException("Bạn không có quyền hủy đơn hàng này", HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Chỉ có thể hủy đơn đang chờ quán xác nhận", HttpStatus.BAD_REQUEST);
        }
        if (cancelReason == null || cancelReason.isBlank()) {
            throw new AppException("Vui lòng nhập lý do hủy đơn", HttpStatus.BAD_REQUEST);
        }

        String normalizedReason = cancelReason.trim();
        if (normalizedReason.length() > 255) {
            throw new AppException("Lý do hủy không được vượt quá 255 ký tự", HttpStatus.BAD_REQUEST);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(normalizedReason);
        OrderResponse response = mapToOrderResponse(orderRepository.save(order));

        try {
            messagingTemplate.convertAndSend(
                    "/topic/shop/" + order.getShop().getId() + "/orders",
                    response
            );
            messagingTemplate.convertAndSend(
                    "/topic/orders/customer/" + order.getUser().getPhone(),
                    response
            );
        } catch (Exception ignored) {
            // Không làm thất bại thao tác hủy nếu kênh thông báo đang gián đoạn.
        }

        return response;
    }

    @Override
    @Transactional
    public void submitOrderReview(UUID orderId, ReviewSubmitRequest request, String phone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));
        if (!order.getUser().getPhone().equals(phone)) {
            throw new AppException("Bạn không có quyền đánh giá đơn hàng này", HttpStatus.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.RECEIVED) {
            throw new AppException("Chỉ đơn hàng đã hoàn thành mới có thể đánh giá", HttpStatus.BAD_REQUEST);
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new AppException("Đơn hàng này đã được đánh giá rồi", HttpStatus.CONFLICT);
        }

        // 1. Save Order/Delivery Review
        if (request.getOrderRating() != null) {
            Review review = Review.builder()
                    .order(order)
                    .user(order.getUser())
                    .rating(request.getOrderRating())
                    .comment(request.getOrderComment())
                    .build();
            reviewRepository.save(review);
        }

        // 2. Save Shop Review
        if (request.getShopRating() != null) {
            ShopReview shopReview = ShopReview.builder()
                    .order(order)
                    .user(order.getUser())
                    .shop(order.getShop())
                    .rating(request.getShopRating())
                    .comment(request.getShopComment())
                    .build();
            shopReviewRepository.save(shopReview);
        }

        // 3. Save Food Reviews
        if (request.getFoodReviews() != null) {
            for (ReviewSubmitRequest.FoodReviewItem item : request.getFoodReviews()) {
                Food food = foodRepository.findById(item.getFoodId())
                        .orElseThrow(() -> new AppException("Không tìm thấy món ăn: " + item.getFoodId(), HttpStatus.NOT_FOUND));
                FoodReview foodReview = FoodReview.builder()
                        .order(order)
                        .user(order.getUser())
                        .food(food)
                        .rating(item.getRating())
                        .comment(item.getComment())
                        .build();
                foodReviewRepository.save(foodReview);
            }
        }
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
        Order order = orderRepository.findByIdForUpdate(orderId)
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
        if ((newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.RECEIVED)
                && order.getCompletedAt() == null) {
            order.setCompletedAt(LocalDateTime.now());
        }
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

    private boolean isShopOpenNow(Shop shop) {
        LocalTime open = shop.getOpenTime();
        LocalTime close = shop.getCloseTime();
        ShopSettings settings = shop.getSettings();

        if (settings != null) {
            DayOfWeek today = LocalDate.now().getDayOfWeek();
            if (today == DayOfWeek.SATURDAY && settings.getSatOpenTime() != null && settings.getSatCloseTime() != null) {
                open = settings.getSatOpenTime();
                close = settings.getSatCloseTime();
            } else if (today == DayOfWeek.SUNDAY && settings.getSunOpenTime() != null && settings.getSunCloseTime() != null) {
                open = settings.getSunOpenTime();
                close = settings.getSunCloseTime();
            } else if (settings.getMonFriOpenTime() != null && settings.getMonFriCloseTime() != null) {
                open = settings.getMonFriOpenTime();
                close = settings.getMonFriCloseTime();
            }
        }

        if (open == null || close == null) {
            return false;
        }

        LocalTime now = LocalTime.now();
        if (open.equals(close)) {
            return true;
        }
        if (close.isAfter(open)) {
            return !now.isBefore(open) && !now.isAfter(close);
        }
        return !now.isBefore(open) || !now.isAfter(close);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAvailableOrdersForDelivery() {
        return orderRepository.findAvailableOrdersForDelivery().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse claimOrder(UUID orderId, String shipperPhone) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));

        User shipper = userRepository.findByPhone(shipperPhone)
                .orElseThrow(() -> new AppException("Không tìm thấy shipper", HttpStatus.NOT_FOUND));

        if (shipper.getRole() != UserRole.SHIPPER) {
            throw new AppException("Chỉ có tài khoản Shipper mới được nhận đơn", HttpStatus.FORBIDDEN);
        }

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException("Đơn hàng phải ở trạng thái ĐÃ XÁC NHẬN mới có thể nhận giao", HttpStatus.BAD_REQUEST);
        }

        if (order.getShipper() != null) {
            throw new AppException("Đơn hàng này đã có shipper khác nhận", HttpStatus.CONFLICT);
        }

        order.setShipper(shipper);
        Order savedOrder = orderRepository.save(order);
        OrderResponse response = mapToOrderResponse(savedOrder);

        // Thông báo cho Student & Vendor qua WebSocket về việc đơn hàng đã được nhận giao
        try {
            String customerDestination = "/topic/orders/customer/" + order.getUser().getPhone();
            messagingTemplate.convertAndSend(customerDestination, response);

            String shopDestination = "/topic/shop/" + order.getShop().getId() + "/orders";
            messagingTemplate.convertAndSend(shopDestination, response);
        } catch (Exception e) {
            // Ignore
        }

        return response;
    }

    @Override
    @Transactional
    public OrderResponse updateShipperLocation(UUID orderId, String shipperPhone, Double latitude, Double longitude) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));

        if (order.getShipper() == null || !order.getShipper().getPhone().equals(shipperPhone)) {
            throw new AppException("Bạn không phải shipper của đơn hàng này", HttpStatus.FORBIDDEN);
        }

        order.setShipperLatitude(latitude);
        order.setShipperLongitude(longitude);
        order.setShipperLocationUpdatedAt(LocalDateTime.now());
        
        Order savedOrder = orderRepository.save(order);
        
        // Broadcast location update
        try {
            Map<String, Object> payload = Map.of(
                "orderId", orderId.toString(),
                "latitude", latitude,
                "longitude", longitude
            );
            messagingTemplate.convertAndSend("/topic/orders/" + orderId + "/location", payload);
        } catch (Exception e) {
            // Ignore
        }

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getShipperActiveOrders(String shipperPhone) {
        return orderRepository.findActiveOrdersByShipper(shipperPhone).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getShipperOrderHistory(String shipperPhone) {
        return orderRepository.findOrderHistoryByShipper(shipperPhone).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private boolean isShopOwnerLocked(Shop shop) {
        return shop.getOwner() != null && Boolean.TRUE.equals(shop.getOwner().getIsLocked());
    }
}
