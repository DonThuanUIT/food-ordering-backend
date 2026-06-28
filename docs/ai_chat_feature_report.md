# Báo Cáo Phân Tích Tính Năng "Trò Chuyện với Trợ lý AI để Gợi ý Món Ăn"

## 1. Kiến trúc tổng quan (Overview)

### Frontend (Client) -> Backend (Spring Boot) -> LLM (Gemini)
Đồng đội của bạn đã triển khai tính năng gợi ý món ăn sử dụng **Google Gemini** (thông qua Gemini API), không phải OpenAI hay Spring AI.

- **LLM sử dụng**: Google Gemini (Gemini API).
- **Model cụ thể**: `gemini-1.5-flash` (được cấu hình trong `application.yml` ở đường dẫn URL).
- **Endpoint API Gemin**: `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`
- **Phương thức gọi**: RESTful API trực tiếp qua `RestTemplate` của Spring, **không** sử dụng thư viện Spring AI.

### Cấu trúc package chính:
```
modules/food/
  ├── controller/AIRecommendationController.java   # REST endpoint gợi ý món ăn
  ├── service/GeminiService.java                   # Core logic gọi Gemini API
  ├── service/FoodServiceImpl.java                 # Gọi Gemini analyze khi tạo món mới
  ├── repository/FoodRepository.java               # Query lấy danh sách món ăn
  └── dto/gemini/
      ├── GeminiRequest.java                       # Request DTO cho Gemini API
      ├── GeminiResponse.java                      # Response DTO từ Gemini API
      ├── GeminiFoodAnalysis.java                  # DTO phân tích món ăn (tags, cuisine, spicyLevel)
      └── GeminiRecommendationMatch.java           # DTO kết quả gợi ý (foodId, reason)
```

---

## 2. Luồng dữ liệu chi tiết (Data Flow Breakdown)

### Bước 1: Nhận Request từ Client

**File**: `AIRecommendationController.java` (dòng 28-77)

- **Endpoint**: `POST /ai/recommend`
- **Query parameter tùy chọn**: `shopId` (UUID) - nếu có sẽ gợi ý trong phạm vi 1 quán, nếu không sẽ gợi ý toàn hệ thống.
- **Request Body (AIRecommendationRequest)**:
```java
public class AIRecommendationRequest {
    @NotBlank(message = "Yêu cầu gợi ý không được để trống")
    private String query;  // Ví dụ: "Tôi muốn ăn đồ cay", "Món gì ngon?"
}
```

### Bước 2: Lấy ngữ cảnh từ Database (Context Retrieval)

**File**: `FoodRepository.java` (dòng 51-70) và `AIRecommendationController.java` (dòng 36-38)

```java
// Nếu có shopId, lọc theo shop
List<Food> availableFoods = (shopId != null) 
        ? foodRepository.findAllAvailableFoodsByShopId(shopId) 
        : foodRepository.findAllAvailableFoods();
```

**Câu query chi tiết trong FoodRepository**:
```sql
-- findAllAvailableFoods():
SELECT f FROM Food f 
JOIN FETCH f.category 
JOIN FETCH f.shop s 
JOIN FETCH s.owner o 
WHERE f.isAvailable = true            -- Món đang bán (Available)
AND s.isActive = true                 -- Quán đang hoạt động
AND s.status = 'APPROVED'             -- Quán đã được Admin duyệt
AND (o.isLocked = false OR o.isLocked IS NULL)  -- Chủ quán không bị khóa
```

**⇒ Kết luận: Có lọc theo điều kiện "Đang bán" (`isAvailable = true`)** và còn kiểm tra thêm trạng thái quán, chủ quán.

### Bước 3: Xây dựng Prompt (Prompt Engineering)

**File**: `GeminiService.java` (dòng 223-237) - Đây là trái tim của tính năng gợi ý.

**System Prompt được viết như sau**:
```
"Bạn là một trợ lý ảo tư vấn ẩm thực thân thiện tại Việt Nam.\n" +
"Khách hàng yêu cầu: \"{userQuery}\".\n\n" +
"Dưới đây là danh sách thực đơn các món ăn đang có sẵn của quán:\n" +
"{menuJsonStr}\n\n" +
"Hãy chọn ra tối đa 3 món phù hợp nhất với yêu cầu trên của khách hàng từ danh sách thực đơn có sẵn ở trên.\n" +
"Với mỗi món được chọn, hãy giải thích cực kỳ ngắn gọn bằng đúng 1 câu tiếng Việt lý do tại sao món này lại phù hợp.\n" +
"Định dạng kết quả trả về bắt buộc phải là một mảng JSON các đối tượng có cấu trúc chính xác như sau:\n" +
"[\n" +
"  { \"foodId\": \"UUID của món ăn\", \"reason\": \"Lý do gợi ý ngắn gọn bằng tiếng Việt...\" }\n" +
"]\n" +
"Chú ý: Nếu không có món nào phù hợp, hãy trả về một mảng trống: []"
```

**Giải thích Prompt**:
1. Thiết lập **Role** cho AI: "trợ lý ảo tư vấn ẩm thực thân thiện tại Việt Nam"
2. **Input**: Câu query của user + danh sách món ăn dạng JSON (chỉ gửi metadata như name, tags, cuisine, spicyLevel - không gửi ảnh để tiết kiệm token)
3. **Ràng buộc**: Chọn tối đa 3 món, giải thích 1 câu tiếng Việt
4. **Output format**: JSON array bắt buộc - dễ dàng parse ở Backend

### Bước 4: Gọi API Gemini

**File**: `GeminiService.java` (dòng 256-279)

```java
private String callGeminiApi(GeminiRequest requestPayload) {
    String url = geminiUrl + "?key=" + apiKey;  // ghép API key vào URL
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestPayload, headers);
    
    try {
        ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(
            url, entity, GeminiResponse.class);
        // Parse response...
    } catch (Exception e) {
        log.error("API call to Gemini failed: " + e.getMessage());
    }
    return null;
}
```

**Cấu hình RestTemplate**:  
- **Không có timeout cấu hình** - dùng `new RestTemplate()` mặc định với timeout vô hạn.
- **Không có Retry mechanism** - nếu lỗi, chỉ log và return null.
- **API Key**: Lấy từ biến môi trường `GEMINI_API_KEY` qua `@Value(${gemini.api-key})` - không hardcode.

**Xử lý Exception**:  
- Nếu API call thất bại hoặc parse lỗi, `callGeminiApi()` trả về `null`.
- Controller có **cơ chế Fallback** (dòng 65-74): nếu Gemini không trả về kết quả, sẽ chạy thuật toán keyword-based fallback (`getKeywordFallbackMatches`) để tìm món phù hợp.

---

## 3. Đánh giá mã nguồn (Code Review)

### 3.1. Tuân thủ Clean Code

| Tiêu chí | Đánh giá | Ghi chú |
|----------|----------|---------|
| **Tách biệt concern** | ✅ Tốt | Controller, Service, Repository, DTO được tách riêng |
| **Đặt tên** | ✅ Tốt | Tên biến, phương thức rõ ràng, có nghĩa |
| **Xử lý Exception** | ⚠️ Trung bình | Có try/catch nhưng chưa có fallback strategy cho Gemini timeout |
| **Logging** | ✅ Tốt | `@Slf4j` được dùng xuyên suốt, log đầy đủ |
| **Validation** | ✅ Tốt | `@Valid` + `@NotBlank` cho request |

**Điểm cần lưu ý**:
- `RestTemplate` được khởi tạo trực tiếp trong class (`new RestTemplate()`) thay vì inject Bean - khó mock và test.
- `FoodScore` là inner class `private static` - tạm chấp nhận được, nhưng nên tách ra DTO riêng.

### 3.2. Lỗ hổng bảo mật

| Vấn đề | Mức độ | Mô tả |
|--------|--------|-------|
| **API Key trong source code** | ❌ KHÔNG có - ✅ **An toàn** | API Key dùng `@Value` với biến môi trường `${GEMINI_API_KEY:}`, không hardcode |
| **API Key lộ trên URL** | ⚠️ **Trung bình** | Key được truyền qua query parameter `?key=` thay vì Header Authorization - có thể bị log ở proxy/server trung gian |
| **Không giới hạn request (Rate Limiting)** | ⚠️ **Cao** | Endpoint `/ai/recommend` không có rate limit, ai cũng có thể gọi - có thể gây tốn kém phí API Gemini |
| **Thiếu Authentication/Authorization** | ⚠️ **Trung bình** | Controller không kiểm tra quyền truy cập (không có `Principal`) - cần kiểm tra SecurityConfig để xem endpoint có được public không |

### 3.3. Đề xuất cải tiến (Optimization Suggestions)

#### 1. **Triển khai Caching cho Context (Menu ngữ cảnh)**
- **Vấn đề**: Mỗi request AI đều query toàn bộ danh sách món ăn và gửi lên Gemini.
- **Giải pháp**: Cache danh sách món ăn theo shopId với TTL ngắn (30-60 giây) bằng Redis hoặc Caffeine Cache. Chỉ refresh cache khi có món mới được thêm/cập nhật.
- **Code mẫu đề xuất**:
  ```java
  @Cacheable(value = "aiMenuContext", key = "#shopId != null ? #shopId : 'all'", unless = "#result.isEmpty()")
  public List<Food> getMenuContext(UUID shopId) { ... }
  ```

#### 2. **Thêm Pagination cho Context và tối ưu Token**
- **Vấn đề**: Nếu hệ thống có hàng trăm món ăn, prompt sẽ rất dài và tốn token.
- **Giải pháp**: 
  - Giới hạn số món gửi lên Gemini (ví dụ: top 30 món phổ biến nhất)
  - Dùng `@EntityGraph` thay vì `JOIN FETCH` để tránh duplicate data
  - Cân nhắc dùng **Embedding** thực thụ (vector database) thay vì gửi JSON menu to

#### 3. **Cấu hình RestTemplate chuyên nghiệp và Retry Pattern**
- **Vấn đề**: RestTemplate hiện tại không có timeout, không có retry.
- **Giải pháp**:
  ```java
  @Bean
  public RestTemplate geminiRestTemplate() {
      SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      factory.setConnectTimeout(5000);   // 5 giây connect
      factory.setReadTimeout(15000);     // 15 giây read
      
      // Thêm Retry interceptor (Spring Retry)
      return new RestTemplate(factory);
  }
  ```
- **Thêm Spring Retry**: Dùng `@Retryable(maxAttempts = 2, backoff = @Backoff(delay = 1000))` cho `callGeminiApi()`

#### 4. **Bổ sung Rate Limiting**
- **Giải pháp**: Dùng Bucket4j hoặc Spring Filter để giới hạn số request AI mỗi user/phút, tránh abuse và tốn phí.

---

## Kết luận chung

Đồng đội của bạn đã xây dựng một tính năng **hoạt động tốt với kiến trúc rõ ràng**, tuân thủ các nguyên tắc Clean Code cơ bản, có xử lý Fallback khi Gemini lỗi, và đặc biệt là **không hardcode API Key**. Tuy nhiên, còn một số điểm có thể cải thiện về performance (caching), bảo mật (rate limiting, API key trên URL), và độ tin cậy (timeout, retry).