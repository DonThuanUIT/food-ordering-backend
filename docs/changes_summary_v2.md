# Tổng Hợp Các Thay Đổi - Phân Trang, Real-time, Đóng Cửa Hàng & Tăng Trưởng Dashboard

Hệ thống backend đã được nâng cấp thành công với các tính năng mới phục vụ quản trị Vendor và tối ưu hóa hiệu năng:

---

## 1. Phân Trang Món Ăn (Food Pagination)
- **Mục tiêu**: Giảm tải dữ liệu truyền tải qua mạng khi Vendor có danh sách món ăn lớn.
- **Giải pháp**: Nâng cấp API `GET /vendor/shops/{shopId}/foods` từ trả về danh sách đầy đủ sang dạng phân trang (`Page<FoodResponse>`).
- **Chi tiết kỹ thuật**:
  - Hỗ trợ tham số phân trang chuẩn của Spring Boot thông qua `@PageableDefault`.
  - Cấu hình custom `countQuery` trong `FoodRepository` để tránh lỗi count khi thực hiện `JOIN FETCH`.

---

## 2. Thông Báo Đơn Hàng Real-time (WebSocket STOMP)
- **Mục tiêu**: Cho phép Vendor nhận đơn hàng mới và cập nhật trạng thái đơn lập tức mà không cần polling từ Frontend.
- **Giải pháp**: Tích hợp gửi sự kiện WebSocket (STOMP) bằng `SimpMessagingTemplate` đã được cấu hình sẵn.
- **Chi tiết kỹ thuật**:
  - Khi khách đặt đơn thành công: Gửi tin nhắn chứa thông tin đơn hàng tới kênh `/topic/shop/{shopId}/orders`.
  - Khi cập nhật trạng thái đơn: Gửi tin nhắn đồng thời tới kênh của Vendor `/topic/shop/{shopId}/orders` và kênh của khách hàng `/topic/orders/customer/{phone}`.

---

## 3. Đóng Cửa Hàng Vĩnh Viễn (Permanently Close Shop)
- **Mục tiêu**: Bảo mật thao tác đóng quán vĩnh viễn, ngăn chặn các sửa đổi trái phép sau khi quán đã đóng.
- **Giải pháp**: Xây dựng quy trình xác thực hai bước (Mật khẩu hoặc OTP gửi qua email) để thực hiện đóng quán.
- **Chi tiết kỹ thuật**:
  - Thêm trạng thái `CLOSED` vào enum `ShopStatus`.
  - API `POST /vendor/shops/{shopId}/close/otp-request`: Tạo OTP lưu vào Redis (hiệu lực 5 phút) và gửi email OTP qua Thymeleaf Template.
  - API `POST /vendor/shops/{shopId}/close`: Xác nhận đóng cửa hàng (kiểm tra mật khẩu qua `PasswordEncoder` hoặc OTP qua Redis). Đặt trạng thái shop thành `CLOSED`, `isActive = false`, `isOpen = false`.
  - Cập nhật `updateShopProfile` và `toggleShopStatus` chặn sửa đổi nếu trạng thái shop đã là `CLOSED`.

---

## 4. Chỉ Số Tăng Trưởng Dashboard (%) (Growth Metric Calculation)
- **Mục tiêu**: Hỗ trợ Vendor so sánh hiệu quả kinh doanh của chu kỳ hiện tại so với chu kỳ trước đó.
- **Giải pháp**: Nâng cấp API `GET /orders/{shopId}/dashboard` để tự động truy vấn dữ liệu chu kỳ trước (cùng số lượng ngày) và tính phần trăm tăng/giảm.
- **Chi tiết kỹ thuật**:
  - Bổ sung 4 trường vào DTO: `revenueGrowth`, `orderCountGrowth`, `completionRateGrowth`, `averageOrderValueGrowth`.
  - Xử lý chia cho 0 an toàn nếu chu kỳ trước chưa có dữ liệu. Làm tròn 2 chữ số thập phân bằng `BigDecimal`.
