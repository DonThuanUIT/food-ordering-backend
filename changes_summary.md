# Tổng Hợp Các Thay Đổi - Cấu Hình Cửa Hàng & Hệ Thống Voucher

Hệ thống đã được nâng cấp và tối ưu hóa thành công để khớp với giao diện cấu hình của Vendor và bổ sung cơ chế khuyến mãi (Voucher) linh hoạt. Dưới đây là chi tiết các thay đổi bằng tiếng Việt.

---

## 1. Thiết Kế Cơ Sở Dữ Liệu Tách Bảng (Tối Ưu Hiệu Năng)

Để tránh bảng `shops` bị quá tải cột (phình to) và nâng cao tốc độ truy vấn danh sách quán ăn cho sinh viên, chúng tôi đã tách cấu hình thành **2 bảng riêng biệt**:
*   **Bảng `shops`**: Chỉ giữ các thông tin cốt lõi (Tên quán, địa chỉ, mô tả, giờ mở/đóng cửa chung, trạng thái kiểm duyệt, và trạng thái đóng/mở tức thời `is_open`).
*   **Bảng phụ `shop_settings` (Quan hệ 1 - 1)**: Lưu trữ toàn bộ các thông tin cài đặt sâu của Vendor bao gồm:
    *   Ảnh bìa (`cover_url`) và Logo (`logo_url`).
    *   Thông tin liên hệ (`email`, `phone` - Support Line).
    *   Thông tin thanh toán ngân hàng (`bank_name`, `bank_account_number`, `bank_account_owner`).
    *   Tùy chọn bật/tắt nhanh (`order_alerts_enabled`, `dorm_promotions_enabled`, `turbo_mode_enabled`).
    *   Giờ mở/đóng cửa chi tiết theo thứ (`mon_fri_open_time`, `mon_fri_close_time`, `sat_open_time`, `sat_close_time`, `sun_open_time`, `sun_close_time`).

---

## 2. Áp Dụng Giờ Mặc Định Khi Đăng Ký Tài Khoản

Nhằm mang lại trải nghiệm tiện lợi nhất cho Vendor khi mới đăng ký tài khoản hoặc tạo quán ăn mới:
*   Khi đăng ký (thông qua `AuthServiceImpl.java`) hoặc tạo mới quán (thông qua `ShopServiceImpl.java`), giờ hoạt động chung (`openTime` và `closeTime`) do Vendor nhập vào sẽ **tự động được sao chép và áp dụng cho tất cả các ngày trong tuần** (Mon-Fri, Thứ Bảy, Chủ Nhật) trong bảng `shop_settings`.
*   Vendor không cần cấu hình thủ công từng ngày mà vẫn có sẵn bộ khung giờ hoạt động đồng bộ và hợp lệ ngay lập tức.

---

## 3. Cơ Chế Khuyến Mãi (Voucher) Linh Hoạt

Hệ thống Voucher mới đã được thiết kế và tích hợp hoàn chỉnh:
*   **Tạo Voucher**: Vendor có thể tự thiết kế mã Voucher riêng cho quán của mình với các tùy chọn:
    *   Loại giảm giá: Theo phần trăm (`PERCENTAGE`) hoặc số tiền cố định (`FIXED_AMOUNT`).
    *   Điều kiện áp dụng: Số tiền đơn hàng tối thiểu (`min_order_value`) và giới hạn số tiền giảm tối đa (`max_discount_value`).
    *   Phạm vi áp dụng: Áp dụng cho toàn bộ Menu (`ALL_MENU`) hoặc chỉ áp dụng cho một số món ăn được chỉ định cụ thể (`SPECIFIC_FOODS`).
*   **Áp dụng Voucher lúc Checkout**: Khi sinh viên mua hàng và nhập mã voucher, Backend sẽ tự động:
    1.  Kiểm tra hạn dùng và trạng thái kích hoạt của voucher.
    2.  Tính tổng tiền của các món ăn hợp lệ trong đơn hàng.
    3.  So sánh với giá trị đơn tối thiểu.
    4.  Tính toán số tiền giảm giá chính xác và trừ vào tổng tiền đơn hàng, đồng thời lưu lịch sử giảm giá (`voucher_code`, `discount_amount`) vào đơn hàng.

---

## 4. Chi Tiết Các File Đã Chỉnh Sửa & Tạo Mới

### Cơ sở dữ liệu:
*   `[MODIFY]` [V7__add_shop_settings_and_vouchers.sql](file:///d:/food-ordering-backend/backend/src/main/resources/db/migration/V7__add_shop_settings_and_vouchers.sql): Script tạo các cột trực tiếp trên bảng `shops` để giữ nguyên checksum lịch sử đã áp dụng lên DB.
*   `[NEW]` [V8__split_shop_settings.sql](file:///d:/food-ordering-backend/backend/src/main/resources/db/migration/V8__split_shop_settings.sql): Script di chuyển các cài đặt sang bảng phụ `shop_settings`, sao chép dữ liệu hiện tại (nếu có) và xóa các cột thừa trên bảng `shops`.

### Các thực thể (JPA Entities):
*   `[MODIFY]` [Shop.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/entity/Shop.java): Thêm trường đóng/mở tức thời `isOpen` và liên kết `@OneToOne` với `ShopSettings`.
*   `[NEW]` [ShopSettings.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/entity/ShopSettings.java): Thực thể lưu trữ các cài đặt mở rộng của cửa hàng.
*   `[MODIFY]` [Order.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/entity/Order.java): Thêm các trường lưu trữ thông tin voucher và số tiền giảm giá của đơn hàng.
*   `[NEW]` [Voucher.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/entity/Voucher.java): Thực thể lưu trữ mã voucher và liên kết nhiều-nhiều với món ăn.

### Cấu trúc truyền dữ liệu (DTOs):
*   `[MODIFY]` [ShopResponse.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/dto/response/ShopResponse.java): Bổ sung toàn bộ các trường cài đặt để trả về cho FE.
*   `[MODIFY]` [ShopDetailResponse.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/dto/response/ShopDetailResponse.java): Trả thêm ảnh bìa, logo và trạng thái mở cửa của quán cho học sinh.
*   `[MODIFY]` [ShopUpdateRequest.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/dto/request/ShopUpdateRequest.java): Gỡ bỏ các ràng buộc bắt buộc để hỗ trợ cập nhật từng phần (cập nhật riêng lẻ từng mục Contact, Payment...).
*   `[MODIFY]` [CheckoutRequest.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/order/dto/request/CheckoutRequest.java) & [OrderResponse.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/order/dto/response/OrderResponse.java): Tích hợp mã voucher và chiết khấu.
*   `[NEW]` [VoucherCreateRequest.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/voucher/dto/request/VoucherCreateRequest.java) & [VoucherResponse.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/voucher/dto/response/VoucherResponse.java): DTO phục vụ cho quản lý voucher.

### Tầng xử lý logic (Services):
*   `[MODIFY]` [AuthServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/auth/AuthServiceImpl.java): Khởi tạo bản ghi cài đặt mặc định với giờ hoạt động Mon-Sun đồng bộ khi Vendor đăng ký.
*   `[MODIFY]` [ShopServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/service/ShopServiceImpl.java): Cập nhật cơ chế đọc/ghi cấu hình thông qua bảng `shop_settings`, cập nhật trạng thái đóng/mở tức thời của quán và tính toán trạng thái hoạt động dựa theo ngày cụ thể trong tuần.
*   `[NEW]` [VoucherServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/voucher/service/VoucherServiceImpl.java): Xử lý nghiệp vụ tạo, cập nhật, xóa và liệt kê voucher cho Vendor / Học sinh.
*   `[MODIFY]` [OrderServiceImpl.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/order/OrderServiceImpl.java): Xử lý kiểm tra điều kiện áp dụng và trừ tiền chiết khấu voucher khi đặt hàng.

### Tầng giao tiếp mạng (Controllers):
*   `[MODIFY]` [VendorShopController.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/shop/controller/VendorShopController.java): Thêm API lấy chi tiết 1 quán ăn phục vụ cho trang Settings (`GET /vendor/shops/{shopId}`), cập nhật API thay đổi trạng thái động.
*   `[NEW]` [VendorVoucherController.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/voucher/controller/VendorVoucherController.java) & [StudentVoucherController.java](file:///d:/food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules/voucher/controller/StudentVoucherController.java): Các API quản lý voucher của cửa hàng.
