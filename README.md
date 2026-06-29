# Food Ordering App (UniEats)
Ứng dụng đặt món trong khuôn viên trường, gồm Android native client và Spring Boot backend theo kiến trúc REST API + WebSocket, dùng PostgreSQL và Flyway để quản lý dữ liệu.
## Tổng quan
* Dự án gồm 2 phần chính:
* `food-ordering-android-app`: ứng dụng Android cho sinh viên, chủ quán, shipper và admin.
* `food-ordering-backend`: backend Spring Boot cung cấp REST API, WebSocket chat, xác thực JWT và tích hợp dịch vụ ngoài.
* Backend dùng Spring Boot 3.3.5, Java 21, Spring Security, Spring Data JPA, Flyway, WebSocket, Redis, Mail, Cloudinary, Firebase Admin và Gemini API.
* Mobile dùng Android native Java/XML, ViewBinding, Retrofit, OkHttp, LiveData/ViewModel, Material Components, Glide, Firebase Messaging, STOMP/RxJava, osmdroid và MPAndroidChart.
* Database dùng PostgreSQL.
* Migration dùng Flyway trong `backend/src/main/resources/db/migration`.
* Mục tiêu production:
* tách secret khỏi source code;
* dùng database, Redis, SMTP, Cloudinary, Firebase và Gemini riêng cho từng môi trường;
* cấu hình domain/TLS và logging phù hợp;
* tắt các cấu hình debug không cần thiết trước khi giao.
## Vai trò sản phẩm
Dự án hiện có 4 vai trò sản phẩm:
* `STUDENT`: xem cửa hàng, xem món, giỏ hàng, đặt món, hủy đơn, theo dõi giao hàng, đánh giá, chat và gợi ý món ăn.
* `VENDOR`: quản lý cửa hàng, danh mục, món ăn, voucher, đơn hàng, cấu hình cửa hàng, đánh giá và thống kê.
* `SHIPPER`: xem đơn có thể giao, nhận đơn, cập nhật trạng thái giao hàng, cập nhật vị trí và xem lịch sử giao.
* `ADMIN`: xem tổng quan, duyệt/quản lý cửa hàng, quản lý người dùng và khóa tài khoản.
Lưu ý: permission/authority như `ROLE_ADMIN`, `ROLE_VENDOR`, `ROLE_STUDENT`, `ROLE_SHIPPER` là lớp kỹ thuật trong Spring Security. Chúng không phải vai trò sản phẩm mới.
## Tech stack
### Backend
* Java 21
* Spring Boot 3.3.5
* Spring Web, Spring Security, Spring Data JPA, Validation
* PostgreSQL JDBC driver
* Flyway
* JWT với `jjwt`
* Redis cho các luồng OTP/cache liên quan
* Spring Mail và Thymeleaf cho email
* Spring WebSocket cho chat/thông báo thời gian thực
* Cloudinary cho upload ảnh
* Firebase Admin cho push notification
* Gemini API cho gợi ý/phân tích món ăn
### Android
* Android native Java/XML
* Min SDK 31, target SDK 36, compile SDK 36
* Java 17
* ViewBinding
* Retrofit 2.11.0, Gson converter, OkHttp logging interceptor
* AndroidX Lifecycle ViewModel/LiveData
* Material Components, AppCompat, ConstraintLayout
* Glide
* Firebase Messaging
* STOMP Protocol Android, RxJava/RxAndroid
* osmdroid cho bản đồ
* MPAndroidChart cho thống kê
* AndroidX Biometric
## Cấu trúc thư mục
* `food-ordering-android-app/`: mã nguồn ứng dụng Android.
* `food-ordering-android-app/app/src/main/java/com/foodorderingapp/data`: API client, repository và tầng truy cập dữ liệu.
* `food-ordering-android-app/app/src/main/java/com/foodorderingapp/model`: request/response/model dùng trong app.
* `food-ordering-android-app/app/src/main/java/com/foodorderingapp/ui`: activity, fragment, adapter và màn hình theo từng luồng.
* `food-ordering-android-app/app/src/main/java/com/foodorderingapp/viewmodel`: ViewModel cho các màn hình.
* `food-ordering-android-app/app/src/main/java/com/foodorderingapp/utils`: TokenManager, constant và tiện ích dùng chung.
* `food-ordering-android-app/app/src/main/res`: layout, drawable, menu, values và resource Android.
* `food-ordering-backend/backend`: Spring Boot project.
* `food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/core`: config, security, exception, enum và util dùng chung.
* `food-ordering-backend/backend/src/main/java/com/foodorderingapp/backend/modules`: các module nghiệp vụ như auth, shop, food, cart, order, chat, voucher, admin, upload, notification.
* `food-ordering-backend/backend/src/main/resources/application.yml`: cấu hình chung.
* `food-ordering-backend/backend/src/main/resources/application-local.yml`: cấu hình local có secret. Không đưa giá trị thật vào README public.
* `food-ordering-backend/backend/src/main/resources/db/migration`: Flyway migration và seed data.
* `.agents/AGENTS.md`: quy tắc làm việc cho Codex/AI assistant trong workspace.
## API chính
* `POST /api/auth/register/student`: đăng ký sinh viên.
* `POST /api/auth/register/vendor`: đăng ký chủ quán.
* `POST /api/auth/register/shipper`: đăng ký shipper.
* `POST /api/auth/login`: đăng nhập bằng số điện thoại và mật khẩu.
* `GET /api/shops`: danh sách cửa hàng public.
* `GET /api/shops/{shopId}/detail-menu`: chi tiết cửa hàng và menu.
* `GET /api/buildings`: danh sách tòa nhà.
* `POST /api/cart/items`: thêm món vào giỏ hàng.
* `POST /api/orders/checkout`: tạo đơn hàng.
* `GET /api/orders/active`: đơn đang xử lý của student.
* `GET /api/orders/available-for-delivery`: đơn có thể nhận giao.
* `POST /api/orders/{orderId}/claim`: shipper nhận đơn.
* `PUT /api/orders/{orderId}/status`: shipper cập nhật trạng thái đơn.
* `GET /api/chat/rooms`: danh sách phòng chat.
* `POST /api/chat/send`: gửi tin nhắn.
* `POST /api/upload/image`: upload ảnh.
* `POST /api/ai/recommend`: gợi ý món ăn.
* `GET /api/admin/overview`: tổng quan admin.
* `GET /api/admin/users`: quản lý người dùng.
* `GET /api/admin/shops`: quản lý cửa hàng.
## Chạy local
### 1. Chuẩn bị backend environment
Backend mặc định kích hoạt profile `local` trong `application.yml`.
Tạo hoặc cập nhật file:
```text
food-ordering-backend/backend/src/main/resources/application-local.yml
```
Không commit secret thật nếu repo dùng công khai. Các giá trị nhạy cảm cần dùng placeholder khi viết tài liệu:
```yaml
spring:
datasource:
url: jdbc:postgresql://<db-host>:<db-port>/<db-name>
username: <db-username>
password: <db-password>
data:
redis:
url: redis://<redis-user>:<redis-password>@<redis-host>:<redis-port>
mail:
username: <smtp-email>
password: <smtp-app-password>
jwt:
secret: <jwt-secret>
cloudinary:
cloud-name: <cloudinary-cloud-name>
api-key: <cloudinary-api-key>
api-secret: <cloudinary-api-secret>
gemini:
api-key: <gemini-api-key>
```
### 2. Chuẩn bị PostgreSQL và Redis
* PostgreSQL cần tồn tại trước khi chạy backend.
* Flyway sẽ tự chạy migration khi backend start.
* Migration `V11__Seed_Test_Data.sql` dùng extension `pgcrypto`, nên database user cần có quyền tạo extension hoặc extension phải được tạo sẵn.
* Redis cần cấu hình đúng nếu dùng OTP, email verification và các luồng liên quan.
* Repo hiện chưa có file `docker-compose.yml`, vì vậy không có lệnh Docker Compose chính thức trong repo.
### 3. Chạy backend
```powershell
cd food-ordering-backend\backend
.\mvnw.cmd spring-boot:run
```
Backend chạy tại:
```text
http://localhost:8080/api
```
### 4. Chuẩn bị Android environment
File `local.properties` của Android Studio nằm trong:
```text
food-ordering-android-app/local.properties
```
Nếu cần dùng Gemini key ở Android build config:
```properties
gemini.api.key=<gemini-api-key>
```
Base URL của app nằm tại:
```text
food-ordering-android-app/app/src/main/java/com/foodorderingapp/utils/constants/AppConstants.java
```
Mặc định:
* Android Emulator gọi backend qua `http://10.0.2.2:8080/api/`.
* Thiết bị thật đang dùng `REAL_DEVICE_BASE_URL`, hiện trỏ tới `USB_REVERSE_BASE_URL`.
* Nếu dùng thiết bị thật qua Wi-Fi, cập nhật IP trong `WIFI_BASE_URL` hoặc `REAL_DEVICE_BASE_URL`.
* Nếu dùng USB reverse, kiểm tra lại `USB_REVERSE_BASE_URL` cho khớp môi trường và chạy:
```powershell
adb reverse tcp:8080 tcp:8080
```
### 5. Build và chạy Android
Build debug APK:
```powershell
cd food-ordering-android-app
.\gradlew.bat assembleDebug
```
Cài lên emulator/thiết bị đang kết nối:
```powershell
cd food-ordering-android-app
.\gradlew.bat installDebug
```
Có thể chạy trực tiếp bằng Android Studio nếu cần debug UI.
### 6. Kiểm tra nhanh dữ liệu test
Kiểm tra endpoint public:
```powershell
curl.exe http://localhost:8080/api/buildings
curl.exe http://localhost:8080/api/shops
```
Kiểm tra login bằng tài khoản seed:
```powershell
curl.exe -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"phone\":\"0111111111\",\"password\":\"[mật khẩu nội bộ]\"}"
```
## Tài khoản test nội bộ
| Vai trò | Số điện thoại | Mật khẩu |
| --- | --- | --- |
| `SHIPPER` | `0935985407` | `Lehuutrung2006@` |
| `VENDOR` | `0166666666` | `123456` |
| `STUDENT` | `0144444444` | `123456` |
| `ADMIN` | `0101111111` | `123456` |
## Dữ liệu test local
Seed data nằm trong các migration Flyway:
* `V11__Seed_Test_Data.sql`:
* 5 student;
* 5 vendor;
* 5 cửa hàng mẫu;
* danh mục, món ăn, bank account;
* giỏ hàng, voucher, đơn hàng và review mẫu.
* `V15__Seed_Admin_Data.sql`:
* 2 tài khoản admin.
* `V18__Seed_Shop5_Real_Food_Data.sql`:
* bổ sung dữ liệu món ăn cho shop.
* `V24__Seed_full_buildings_data.sql`:
* 16 tòa nhà có tọa độ.
* `V25__Seed_Shipper_Test_Orders.sql`:
* 3 đơn `CONFIRMED` cho shipper test nhận giao;
* order id mẫu: `00000000-0000-0000-0000-000000000820`, `00000000-0000-0000-0000-000000000821`, `00000000-0000-0000-0000-000000000822`.
Voucher mẫu:
* `KOI10`
* `KOIFREE`
* `BANHMI20`
* `CAY15`
Shop mẫu:
* Trà Sữa KOI
* Bánh Mì Que
* Cơm Gà Xối Mỡ
* Phở Bò Hà Nội
* Mì Cay Hàn Quốc
## Production và triển khai
Repo hiện chưa có các file triển khai production như:
* `Dockerfile`
* `docker-compose.yml`
* GitHub Actions workflow
* Bicep/Terraform
* Coolify config
* script provision server
Trước khi triển khai production cần chuẩn bị riêng:
* runtime cho backend Java 21;
* PostgreSQL production;
* Redis production;
* SMTP account;
* Cloudinary credential;
* Firebase service account và `google-services.json` cho Android;
* Gemini API key;
* domain, TLS và reverse proxy;
* chính sách backup database.
### Secret production bắt buộc
Không ghi giá trị thật vào README, issue tracker hoặc commit history.
* `SPRING_DATASOURCE_URL`
* `SPRING_DATASOURCE_USERNAME`
* `SPRING_DATASOURCE_PASSWORD`
* `SPRING_DATA_REDIS_URL`
* `SPRING_MAIL_USERNAME`
* `SPRING_MAIL_PASSWORD`
* `JWT_SECRET`
* `CLOUDINARY_CLOUD_NAME`
* `CLOUDINARY_API_KEY`
* `CLOUDINARY_API_SECRET`
* `GEMINI_API_KEY`
* Firebase service account cho backend
* `google-services.json` cho Android app
## Kiểm thử trước khi giao
Backend:
```powershell
cd food-ordering-backend\backend
.\mvnw.cmd test
```
Android unit test:
```powershell
cd food-ordering-android-app
.\gradlew.bat testDebugUnitTest
```
Android instrumented test:
```powershell
cd food-ordering-android-app
.\gradlew.bat connectedDebugAndroidTest
```
Build Android debug:
```powershell
cd food-ordering-android-app
.\gradlew.bat assembleDebug
```
Smoke check backend:
```powershell
curl.exe http://localhost:8080/api/buildings
curl.exe http://localhost:8080/api/shops
```
## Ghi chú vận hành
* `application-local.yml` và `local.properties` có thể chứa secret local. Không đưa giá trị thật lên README public.
* JWT token được gửi từ Android qua header `Authorization: Bearer <token>`.
* WebSocket chat dùng URL suy ra từ `BASE_URL` và endpoint `ws-chat`.
* Firebase Messaging chỉ được kích hoạt khi Android project có `google-services.json`.
* Cloudinary cần dùng cho upload ảnh của món ăn, cửa hàng hoặc avatar.
* Redis và email cần hoạt động đúng để các luồng OTP/xác thực email không bị lỗi.
* Voucher có ngày bắt đầu/kết thúc và trạng thái active; cần kiểm tra lại khi test dữ liệu cũ.
* Các trạng thái đơn hàng chính gồm `PENDING`, `CONFIRMED`, `DELIVERING`, `RECEIVED`, `FAILED`, `REJECTED`, `COMPLETED`, `CANCELLED`.
* Admin không được phép khóa tài khoản admin khác theo logic backend hiện tại.

