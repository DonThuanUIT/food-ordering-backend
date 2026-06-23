# Kế hoạch triển khai nâng cấp API backend (Cập nhật đồng bộ cấu trúc dự án)

Tài liệu này mô tả chi tiết phương án kỹ thuật và kế hoạch thay đổi mã nguồn backend (Spring Boot) cho 4 tính năng, được thiết kế đồng bộ hoàn toàn với các Design Pattern hiện có trong dự án:
- Sử dụng **Lombok Builders** cho DTOs và Entities.
- Sử dụng **Pageable / Page<T>** của Spring Data JPA cho phân trang.
- Sử dụng **AppException** (nằm trong `core.exception`) để ném lỗi nghiệp vụ.
- Xác thực người dùng bằng **Spring Security Principal** (`principal.getName()` để lấy số điện thoại của User).
- Sử dụng **PasswordEncoder** (BCrypt) để kiểm tra mật khẩu.
- Tích hợp **WebSocket STOMP** thông qua `SimpMessagingTemplate` đã được cấu hình.
- Sử dụng **Redis Template** qua `OtpService` để xử lý mã OTP.

---

## User Review Required

> [!IMPORTANT]
> **Phương án Real-time đơn hàng:**
> Dự án đã cấu hình sẵn cơ sở hạ tầng WebSocket sử dụng giao thức STOMP (`WebSocketConfig`, `WebSocketAuthInterceptor` xác thực qua JWT Bearer token). Do đó, việc triển khai **WebSocket ở backend** là phương án tối ưu, giúp:
> - Tiết kiệm tài nguyên server (tránh hàng ngàn request polling rác từ client gửi liên tục mỗi 20s).
> - Nhận thông báo tức thời ngay khi khách đặt đơn mới hoặc khi chủ quán cập nhật trạng thái đơn.
> - *Khuyến nghị:* Chọn phương án dùng WebSocket ở backend thay vì Polling ở frontend.

> [!IMPORTANT]
> **Ảnh hưởng của Phân trang món ăn:**
> API lấy danh sách món ăn của Vendor `/api/vendor/shops/{shopId}/foods` sẽ chuyển định dạng trả về từ `List<FoodResponse>` sang `Page<FoodResponse>` (chứa danh sách món ăn kèm thông tin phân trang `totalPages`, `totalElements`, `size`, `number`, v.v.). Frontend cần cập nhật để đọc mảng dữ liệu từ trường `.content` của response.

---

## Proposed Changes

### 1. Phân trang món ăn (Food Pagination)

#### [MODIFY] [FoodRepository.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/food/repository/FoodRepository.java)
- Thêm phương thức truy vấn có phân trang sử dụng `Pageable` và tùy biến `countQuery` để tối ưu hiệu năng tránh lỗi `JOIN FETCH` count:
```java
    @Query(value = "SELECT f FROM Food f JOIN FETCH f.category WHERE f.shop.id = :shopId " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId)",
           countQuery = "SELECT COUNT(f) FROM Food f WHERE f.shop.id = :shopId " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId)")
    org.springframework.data.domain.Page<Food> findByShopIdAndOptionalCategory(
            @Param("shopId") UUID shopId,
            @Param("categoryId") UUID categoryId,
            org.springframework.data.domain.Pageable pageable);
```

#### [MODIFY] [FoodService.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/food/service/FoodService.java)
- Thay đổi chữ ký hàm `getAllFoods` để nhận `Pageable` và trả về `Page<FoodResponse>`:
```java
    org.springframework.data.domain.Page<FoodResponse> getAllFoods(UUID shopId, UUID categoryId, String vendorPhone, org.springframework.data.domain.Pageable pageable);
```

#### [MODIFY] [FoodServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/food/service/FoodServiceImpl.java)
- Cập nhật logic hàm `getAllFoods` để gọi repository phân trang và map kết quả qua `mapToResponse`:
```java
    @Override
    @Transactional(readOnly = true)
    public Page<FoodResponse> getAllFoods(UUID shopId, UUID categoryId, String vendorPhone, Pageable pageable) {
        validateShopOwnership(shopId, vendorPhone);

        if (categoryId != null) {
            validateCategoryBelongsToShop(categoryId, shopId);
        }

        return foodRepository.findByShopIdAndOptionalCategory(shopId, categoryId, pageable)
                .map(this::mapToResponse);
    }
```

#### [MODIFY] [FoodController.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/food/controller/FoodController.java)
- Cập nhật endpoint GET `/vendor/shops/{shopId}/foods` để chấp nhận tham số phân trang qua `@PageableDefault` (mặc định size = 10, sắp xếp theo tên):
```java
    @GetMapping("/vendor/shops/{shopId}/foods")
    public ResponseEntity<Page<FoodResponse>> getAllFoods(
            @PathVariable UUID shopId,
            @RequestParam(required = false) UUID categoryId,
            @org.springframework.data.web.PageableDefault(size = 10) Pageable pageable,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        return ResponseEntity.ok(foodService.getAllFoods(shopId, categoryId, vendorPhone, pageable));
    }
```

---

### 2. Real-time đơn hàng (Real-time Order Updates via WebSocket)

#### [MODIFY] [OrderServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/order/OrderServiceImpl.java)
- Tích hợp `SimpMessagingTemplate` để bắn tin nhắn real-time:
  - Khi **đặt đơn hàng thành công** (`createOrder`): 
    - Gửi tin nhắn chứa thông tin `OrderResponse` đến topic kênh `/topic/shop/{shopId}/orders` của cửa hàng liên quan để màn hình quản trị của Vendor cập nhật thời gian thực.
  - Khi **cập nhật trạng thái đơn** (`updateOrderStatus`):
    - Gửi thông báo đến `/topic/shop/{shopId}/orders` (đồng bộ giao diện Vendor).
    - Gửi thông báo đến `/topic/orders/customer/{customerPhone}` (hoặc `/topic/orders/{orderId}`) của Sinh viên để hiển thị trạng thái đơn hàng thời gian thực.

---

### 3. Đóng cửa hàng vĩnh viễn (Permanently Close Shop)

#### [MODIFY] [ShopStatus.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/core/enums/ShopStatus.java)
- Thêm trạng thái `CLOSED` vào enum `ShopStatus` để đánh dấu cửa hàng đã đóng vĩnh viễn.

#### [NEW] [ShopCloseRequest.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/dto/request/ShopCloseRequest.java)
- Tạo DTO nhận yêu cầu đóng quán, sử dụng các annotation validation của Jakarta:
```java
package com.foodorderingapp.backend.modules.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopCloseRequest {
    @NotBlank(message = "Phương thức xác thực (PASSWORD hoặc OTP) không được để trống")
    private String verificationType; // "PASSWORD" hoặc "OTP"
    private String password;
    private String otpCode;
}
```

#### [NEW] [close-shop-otp-email.html](file:///d:/food-ordering-backend/backend/src/main/resources/templates/close-shop-otp-email.html)
- Tạo template email Thymeleaf để gửi mã OTP xác nhận đóng cửa hàng (màu sắc cảnh báo đỏ để tránh thao tác nhầm lẫn).

#### [MODIFY] [EmailService.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/email/EmailService.java)
- Thêm hàm `@Async public void sendCloseShopOtpEmail(String toEmail, String shopName, String otpCode)` sử dụng Thymeleaf TemplateEngine để gửi email mã OTP.

#### [MODIFY] [ShopService.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/service/ShopService.java)
- Bổ sung 2 phương thức vào interface:
```java
    void requestCloseShopOtp(UUID shopId, String vendorPhone);
    void confirmCloseShop(UUID shopId, ShopCloseRequest request, String vendorPhone);
```

#### [MODIFY] [ShopServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/service/ShopServiceImpl.java)
- Triển khai logic đóng cửa hàng:
  - `requestCloseShopOtp`: Xác thực quyền sở hữu quán ăn thông qua `shopValidationComponent.validateAndGetShop`, tạo OTP lưu vào Redis thông qua `otpService.generateAndSaveOtp(email)` (hoặc tùy biến key prefix `otp:close:`), gửi email OTP.
  - `confirmCloseShop`:
    - Xác thực bằng mật khẩu (sử dụng `passwordEncoder.matches(request.getPassword(), owner.getPassword())`).
    - Hoặc xác thực bằng OTP (sử dụng `otpService.validateOtp(owner.getEmail(), request.getOtpCode())`).
    - Nếu xác thực thành công, đặt `shop.setStatus(ShopStatus.CLOSED)`, `shop.setIsActive(false)`, `shop.setIsOpen(false)` và cập nhật Settings của shop.
  - Sửa đổi `updateShopProfile` và `toggleShopStatus`: Ném ra `AppException(..., HttpStatus.BAD_REQUEST)` nếu cửa hàng có trạng thái là `CLOSED` để chặn đứng mọi thay đổi sau khi đóng quán vĩnh viễn.

#### [MODIFY] [VendorShopController.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/controller/VendorShopController.java)
- Thêm các endpoint REST phục vụ nghiệp vụ đóng cửa hàng:
  - `POST /vendor/shops/{shopId}/close/otp-request`: Yêu cầu gửi OTP đóng cửa hàng qua email.
  - `POST /vendor/shops/{shopId}/close`: Xác nhận đóng cửa hàng vĩnh viễn (chấp nhận mật khẩu hoặc OTP).

---

### 4. Tính toán Chỉ số Tăng trưởng (%) (Growth Metric Calculation)

#### [MODIFY] [VendorDashboardDto.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/order/dto/response/VendorDashboardDto.java)
- Bổ sung 4 trường dữ liệu tăng trưởng:
```java
    private Double revenueGrowth;           // Tăng trưởng doanh thu (%)
    private Double orderCountGrowth;        // Tăng trưởng số lượng đơn (%)
    private Double completionRateGrowth;    // Tăng trưởng tỷ lệ hoàn thành (%)
    private Double averageOrderValueGrowth; // Tăng trưởng AOV (%)
```

#### [MODIFY] [OrderServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/order/OrderServiceImpl.java)
- Nâng cấp phương thức `getVendorDashboard`:
  1. Xác định thời khoảng chu kỳ hiện tại: `duration = Duration.between(startDate, endDate)`.
  2. Xác định mốc thời gian chu kỳ liền trước: `previousStartDate = startDate.minus(duration)` và `previousEndDate = startDate`.
  3. Lấy dữ liệu của chu kỳ trước: `VendorDashboardResponse previousOverview = orderRepository.getVendorDashboardStats(shopId, previousStartDate, previousEndDate)`.
  4. Tính toán mức tăng trưởng phần trăm an toàn (tránh lỗi Division by Zero khi số liệu kỳ trước bằng 0):
     - Công thức chung: `((current - previous) / previous) * 100`.
     - Làm tròn kết quả trả về sử dụng `BigDecimal` với scale = 2.
  5. Đóng gói và trả về trong `VendorDashboardDto`.

---

## Verification Plan

### Automated Tests
Chạy thử nghiệm kiểm tra biên dịch và kiểm tra tính toàn vẹn của mã nguồn:
```powershell
mvn clean test
```

### Manual Verification
- **Phân trang món ăn**: Dùng Postman gọi GET `/api/vendor/shops/{shopId}/foods?page=0&size=5` kiểm tra cấu trúc dữ liệu trả về xem có chứa các trường `content`, `totalPages`, `totalElements` và giới hạn đúng 5 kết quả hay không.
- **Real-time đơn hàng**: Kết nối client WebSocket STOMP tới địa chỉ `/api/ws-chat`, đăng ký lắng nghe kênh `/topic/shop/{shopId}/orders`. Thực hiện đặt hàng từ khách hàng, kiểm tra xem client STOMP của cửa hàng có lập tức nhận được message chứa thông tin đơn hàng hay không.
- **Đóng cửa hàng vĩnh viễn**:
  - Gửi yêu cầu lấy OTP đóng cửa hàng, kiểm tra email nhận được OTP.
  - Xác nhận đóng cửa hàng bằng mật khẩu/OTP sai -> Ném lỗi 400/401.
  - Xác nhận đóng cửa hàng bằng mật khẩu/OTP đúng -> Đóng quán thành công. Thử gọi API sửa thông tin quán để đảm bảo hệ thống chặn thành công.
- **Dashboard tăng trưởng**: Gọi API `/api/orders/{shopId}/dashboard` với các khoảng thời gian khác nhau để xác nhận phần trăm tăng trưởng hiển thị chính xác.
