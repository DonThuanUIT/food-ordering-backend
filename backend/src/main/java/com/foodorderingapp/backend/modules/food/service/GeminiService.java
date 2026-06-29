package com.foodorderingapp.backend.modules.food.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.modules.food.dto.gemini.*;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    private final FoodRepository foodRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-key}")
    private String apiKey;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Value("${gemini.url}")
    private String geminiUrl;

    private byte[] downloadImageBytes(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        try {
            org.springframework.http.client.SimpleClientHttpRequestFactory requestFactory = 
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(2000);
            requestFactory.setReadTimeout(3000);
            RestTemplate tempTemplate = new RestTemplate(requestFactory);
            return tempTemplate.getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            log.warn("Failed to download image from URL: " + imageUrl + ", error: " + e.getMessage());
            return null;
        }
    }

    private String getMimeTypeFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "image/jpeg";
        }
        String urlLower = url.toLowerCase();
        if (urlLower.endsWith(".png")) {
            return "image/png";
        } else if (urlLower.endsWith(".webp")) {
            return "image/webp";
        } else if (urlLower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }

    @Transactional
    public void analyzeFoodSync(UUID foodId) {
        log.info("Starting synchronous food analysis for foodId: {}", foodId);
        Optional<Food> foodOpt = foodRepository.findById(foodId);
        if (foodOpt.isEmpty()) {
            log.warn("Food not found for analysis with ID: {}", foodId);
            return;
        }
        Food food = foodOpt.get();
        try {
            byte[] imageBytes = downloadImageBytes(food.getImageUrl());
            String base64Image = null;
            String mimeType = "image/jpeg";
            if (imageBytes != null && imageBytes.length > 0) {
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
                mimeType = getMimeTypeFromUrl(food.getImageUrl());
                log.info("Successfully downloaded and base64-encoded image for foodId: {}, size: {} bytes", foodId, imageBytes.length);
            }

            // Build prompt
            String prompt = String.format(
                "Hãy phân tích món ăn có tên là \"%s\" (Mô tả hiện tại: \"%s\"). " +
                "Nếu có hình ảnh đính kèm, hãy quan sát kỹ hình ảnh món ăn thực tế để đưa ra đánh giá chính xác nhất. " +
                "Hãy trích xuất và trả về kết quả tuân thủ nghiêm ngặt các quy tắc sau: " +
                "1. Danh sách TỐI THIỂU 5 thẻ, tối đa 10 thẻ (tags) tiếng Việt viết thường, không dấu cách, ngăn cách bằng dấu gạch ngang nếu là từ ghép. Bắt buộc phải có ít nhất 5 tags. " +
                "2. Quy tắc logic bắt buộc cho tags: " +
                "   - Nếu là món nước (cháo, súp, canh, lẩu, bún/phở/mì nước...) -> BẮT BUỘC có tag \"mon-nuoc\", TUYỆT ĐỐI KHÔNG có tag \"mon-kho\". " +
                "   - Nếu là món khô/trộn (mì trộn, hủ tiếu khô, cơm, xôi, bánh mì, hamburger, kimbap...) -> BẮT BUỘC có tag \"mon-kho\" hoặc các tag liên quan như \"com\", \"banh-mi\", TUYỆT ĐỐI KHÔNG có tag \"mon-nuoc\". " +
                "   - Nếu là nước uống (trà, coca, pepsi, sting, nước ép, sinh tố...) -> BẮT BUỘC có tag \"do-uong\" và \"giai-khat\", TUYỆT ĐỐI KHÔNG có tag \"mon-nuoc\", \"mon-kho\", \"an-trua\", \"an-sang\". " +
                "   - Nếu là tráng miệng/ăn vặt (pudding, chè, kem, thạch, khúc bạch, bimbim...) -> BẮT BUỘC có tag \"trang-mieng\" hoặc \"do-an-vat\", TUYỆT ĐỐI KHÔNG có tag \"mon-nuoc\", \"mon-kho\". " +
                "   - Nếu là món thêm/ăn kèm/topping (quẩy, trứng ốp la thêm, mỡ hành, rau thêm, tương thêm...) -> BẮT BUỘC có tag \"topping\", \"mon-kem\" hoặc \"mon-phu\", TUYỆT ĐỐI KHÔNG có tag bữa ăn như \"an-trua\", \"an-sang\". " +
                "3. Vùng miền ẩm thực phù hợp nhất (ví dụ: \"Miền Trung\", \"Miền Bắc\", \"Miền Nam\", \"Tây Âu\", \"Hàn Quốc\", \"Nhật Bản\", \"Đồ ăn nhanh\", \"Đồ uống\"). " +
                "4. Cấp độ cay (từ 0 đến 3, trong đó 0 là không cay, 1 là cay nhẹ, 2 là cay vừa, 3 là rất cay). " +
                "5. Gợi ý mô tả ngắn gọn, hấp dẫn bằng tiếng Việt (khoảng 15-30 từ). " +
                "Định dạng kết quả trả về bắt buộc phải là một đối tượng JSON hợp lệ có dạng: " +
                "{\"tags\":[\"tag1\",\"tag2\"],\"cuisine\":\"Tên Vùng Miền\",\"spicyLevel\":1,\"suggestedDescription\":\"Mô tả ngắn...\"}",
                food.getName(),
                food.getDescription() != null ? food.getDescription() : ""
            );

            GeminiRequest requestPayload;
            if (base64Image != null) {
                List<GeminiRequest.Part> parts = new ArrayList<>();
                parts.add(GeminiRequest.Part.builder()
                        .inlineData(GeminiRequest.InlineData.builder()
                                .mimeType(mimeType)
                                .data(base64Image)
                                .build())
                        .build());
                parts.add(GeminiRequest.Part.builder().text(prompt).build());

                GeminiRequest.Content content = GeminiRequest.Content.builder().parts(parts).build();
                requestPayload = GeminiRequest.builder()
                        .contents(Collections.singletonList(content))
                        .generationConfig(GeminiRequest.GenerationConfig.builder()
                                .responseMimeType("application/json")
                                .build())
                        .build();
            } else {
                requestPayload = GeminiRequest.fromPrompt(prompt, true);
            }

            String responseText = callGeminiApi(requestPayload);
            if (responseText == null || responseText.isBlank()) {
                log.warn("Gemini returned empty response for food: {}", food.getName());
                useFallback(food);
                return;
            }

            String cleanedResponse = responseText.trim();
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
            }

            GeminiFoodAnalysis analysis = objectMapper.readValue(cleanedResponse, GeminiFoodAnalysis.class);
            if (analysis != null) {
                if (analysis.getTags() != null) {
                    food.setTags(analysis.getTags());
                }
                if (analysis.getCuisine() != null) {
                    food.setCuisine(analysis.getCuisine());
                }
                if (analysis.getSpicyLevel() != null) {
                    food.setSpicyLevel(analysis.getSpicyLevel());
                }
                if (analysis.getSuggestedDescription() != null) {
                    food.setDescription(analysis.getSuggestedDescription());
                }
                foodRepository.save(food);
                log.info("Successfully analyzed and updated food {} with tags: {}, cuisine: {}, spicyLevel: {}", 
                    food.getName(), food.getTags(), food.getCuisine(), food.getSpicyLevel());
            } else {
                useFallback(food);
            }
        } catch (Exception e) {
            log.error("Error analyzing food with Gemini for foodId: " + foodId + ", using fallback.", e);
            useFallback(food);
        }
    }

    @Async
    @Transactional
    public void analyzeFoodAsync(UUID foodId) {
        log.info("Starting asynchronous food analysis for foodId: {}", foodId);
        Optional<Food> foodOpt = foodRepository.findById(foodId);
        if (foodOpt.isEmpty()) {
            log.warn("Food not found for analysis with ID: {}", foodId);
            return;
        }
        Food food = foodOpt.get();
        try {
            byte[] imageBytes = downloadImageBytes(food.getImageUrl());
            String base64Image = null;
            String mimeType = "image/jpeg";
            if (imageBytes != null && imageBytes.length > 0) {
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
                mimeType = getMimeTypeFromUrl(food.getImageUrl());
                log.info("Successfully downloaded and base64-encoded image for foodId: {}, size: {} bytes", foodId, imageBytes.length);
            }

            // Build prompt
            String prompt = String.format(
                "Hãy phân tích món ăn có tên là \"%s\" (Mô tả hiện tại: \"%s\"). " +
                "Nếu có hình ảnh đính kèm, hãy quan sát kỹ hình ảnh món ăn thực tế để đưa ra đánh giá chính xác nhất. " +
                "Hãy trích xuất và trả về kết quả tuân thủ nghiêm ngặt các quy tắc sau: " +
                "1. Danh sách TỐI THIỂU 5 thẻ, tối đa 10 thẻ (tags) tiếng Việt viết thường, không dấu cách, ngăn cách bằng dấu gạch ngang nếu là từ ghép. Bắt buộc phải có ít nhất 5 tags. " +
                "2. Quy tắc logic bắt buộc cho tags: " +
                "   - Nếu là món nước (cháo, súp, canh, lẩu, bún/phở/mì nước...) -> BẮT BUỘC có tag \"mon-nuoc\", TUYỆT ĐỐI KHÔNG có tag \"mon-kho\". " +
                "   - Nếu là món khô/trộn (mì trộn, hủ tiếu khô, cơm, xôi, bánh mì, hamburger, kimbap...) -> BẮT BUỘC có tag \"mon-kho\" hoặc các tag liên quan như \"com\", \"banh-mi\", TUYỆT ĐỐI KHÔNG có tag \"mon-nuoc\". " +
                "   - Nếu là nước uống (trà, coca, pepsi, sting, nước ép, sinh tố...) -> BẮT BUỘC có tag \"do-uong\" và \"giai-khat\", TUYỆT ĐỐI KHÔNG có tag \"mon-nuoc\", \"mon-kho\", \"an-trua\", \"an-sang\". " +
                "   - Nếu là tráng miệng/ăn vặt (pudding, chè, kem, thạch, khúc bạch, bimbim...) -> BẮT BUỘC có tag \"trang-mieng\" hoặc \"do-an-vat\", TUYỆT ĐỐI KHÔNG có tag \"mon-nuoc\", \"mon-kho\". " +
                "   - Nếu là món thêm/ăn kèm/topping (quẩy, trứng ốp la thêm, mỡ hành, rau thêm, tương thêm...) -> BẮT BUỘC có tag \"topping\", \"mon-kem\" hoặc \"mon-phu\", TUYỆT ĐỐI KHÔNG có tag bữa ăn như \"an-trua\", \"an-sang\". " +
                "3. Vùng miền ẩm thực phù hợp nhất (ví dụ: \"Miền Trung\", \"Miền Bắc\", \"Miền Nam\", \"Tây Âu\", \"Hàn Quốc\", \"Nhật Bản\", \"Đồ ăn nhanh\", \"Đồ uống\"). " +
                "4. Cấp độ cay (từ 0 đến 3, trong đó 0 là không cay, 1 là cay nhẹ, 2 là cay vừa, 3 là rất cay). " +
                "5. Gợi ý mô tả ngắn gọn, hấp dẫn bằng tiếng Việt (khoảng 15-30 từ). " +
                "Định dạng kết quả trả về bắt buộc phải là một đối tượng JSON hợp lệ có dạng: " +
                "{\"tags\":[\"tag1\",\"tag2\"],\"cuisine\":\"Tên Vùng Miền\",\"spicyLevel\":1,\"suggestedDescription\":\"Mô tả ngắn...\"}",
                food.getName(),
                food.getDescription() != null ? food.getDescription() : ""
            );

            GeminiRequest requestPayload;
            if (base64Image != null) {
                List<GeminiRequest.Part> parts = new ArrayList<>();
                parts.add(GeminiRequest.Part.builder()
                        .inlineData(GeminiRequest.InlineData.builder()
                                .mimeType(mimeType)
                                .data(base64Image)
                                .build())
                        .build());
                parts.add(GeminiRequest.Part.builder().text(prompt).build());

                GeminiRequest.Content content = GeminiRequest.Content.builder().parts(parts).build();
                requestPayload = GeminiRequest.builder()
                        .contents(Collections.singletonList(content))
                        .generationConfig(GeminiRequest.GenerationConfig.builder()
                                .responseMimeType("application/json")
                                .build())
                        .build();
            } else {
                requestPayload = GeminiRequest.fromPrompt(prompt, true);
            }

            String responseText = callGeminiApi(requestPayload);
            if (responseText == null || responseText.isBlank()) {
                log.warn("Gemini returned empty response for food: {}", food.getName());
                useFallback(food);
                return;
            }

            String cleanedResponse = responseText.trim();
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
            }

            GeminiFoodAnalysis analysis = objectMapper.readValue(cleanedResponse, GeminiFoodAnalysis.class);
            if (analysis != null) {
                if (analysis.getTags() != null && (food.getTags() == null || food.getTags().isEmpty())) {
                    food.setTags(analysis.getTags());
                }
                if (analysis.getCuisine() != null && (food.getCuisine() == null || food.getCuisine().isBlank())) {
                    food.setCuisine(analysis.getCuisine());
                }
                if (analysis.getSpicyLevel() != null && food.getSpicyLevel() == 0) {
                    food.setSpicyLevel(analysis.getSpicyLevel());
                }
                // Only overwrite description if currently empty or blank
                if ((food.getDescription() == null || food.getDescription().isBlank()) 
                        && analysis.getSuggestedDescription() != null) {
                    food.setDescription(analysis.getSuggestedDescription());
                }
                foodRepository.save(food);
                log.info("Successfully analyzed and updated food {} with tags: {}, cuisine: {}, spicyLevel: {}", 
                    food.getName(), food.getTags(), food.getCuisine(), food.getSpicyLevel());
            } else {
                useFallback(food);
            }
        } catch (Exception e) {
            log.error("Error analyzing food with Gemini for foodId: " + foodId, e);
            useFallback(food);
        }
    }

    private void useFallback(Food food) {
        log.info("Using fallback for food: {}", food.getName());
        try {
            List<String> tags = new ArrayList<>();
            String nameLower = food.getName().toLowerCase();
            String cuisine = "Việt Nam";
            int spicyLevel = 0;

            // 1. TOPPING / MÓN THÊM / GIA VỊ / RAU THÊM
            boolean isTopping = nameLower.contains("thêm") || nameLower.contains("topping") || nameLower.contains("quẩy") || 
                               nameLower.contains("ốp la") || nameLower.contains("lòng thêm") || nameLower.contains("bò thêm") || 
                               nameLower.contains("gà thêm") || nameLower.contains("trân châu") || nameLower.contains("pudding");

            // 2. ĐỒ UỐNG / NƯỚC GIẢI KHÁT
            boolean isDrink = nameLower.contains("trà") || nameLower.contains("tra") || nameLower.contains("nước") || 
                              nameLower.contains("coca") || nameLower.contains("pepsi") || nameLower.contains("sting") || 
                              nameLower.contains("7 up") || nameLower.contains("7up") || nameLower.contains("lavie") || 
                              nameLower.contains("sữa") || nameLower.contains("sting") || nameLower.contains("cam") || 
                              nameLower.contains("dừa") || nameLower.contains("đậu");

            // 3. TRÁNG MIỆNG / ĂN VẶT
            boolean isDessert = nameLower.contains("pudding") || nameLower.contains("chè") || nameLower.contains("kem") || 
                                nameLower.contains("thạch") || nameLower.contains("khúc bạch") || nameLower.contains("snack") || 
                                nameLower.contains("bim bim") || nameLower.contains("bimbim") || nameLower.contains("đào miếng") ||
                                nameLower.contains("bánh bao hoàng kim") || nameLower.contains("bánh bao lá dứa") || nameLower.contains("da gà");

            if (isTopping) {
                tags.add("topping");
                tags.add("mon-phu");
                tags.add("mon-kem");
                if (nameLower.contains("trứng") || nameLower.contains("ốp la")) {
                    tags.add("trung");
                    tags.add("bo-duong");
                } else if (nameLower.contains("rau")) {
                    tags.add("rau");
                    tags.add("chat-xao");
                } else if (nameLower.contains("trân châu") || nameLower.contains("pudding")) {
                    tags.add("trang-mieng");
                    tags.add("do-ngot");
                } else {
                    tags.add("an-kem");
                }
                tags.add("tien-loi");
                cuisine = "Việt Nam";
            } else if (isDrink) {
                tags.add("do-uong");
                tags.add("giai-khat");
                tags.add("thuc-uong");
                if (nameLower.contains("sữa") || nameLower.contains("milktea")) {
                    tags.add("tra-sua");
                    tags.add("ngot-beo");
                } else if (nameLower.contains("chanh") || nameLower.contains("tắc") || nameLower.contains("mận") || nameLower.contains("cam")) {
                    tags.add("tra-trai-cay");
                    tags.add("chua-ngot");
                } else {
                    tags.add("lanh");
                }
                tags.add("giai-nhiet");
                cuisine = "Đồ uống";
            } else if (isDessert) {
                tags.add("trang-mieng");
                tags.add("do-an-vat");
                tags.add("an-vat");
                if (nameLower.contains("pudding") || nameLower.contains("khúc bạch") || nameLower.contains("chè") || nameLower.contains("kem")) {
                    tags.add("do-ngot");
                    tags.add("mem-min");
                } else if (nameLower.contains("snack") || nameLower.contains("da gà") || nameLower.contains("bim")) {
                    tags.add("gion");
                    tags.add("an-choi");
                } else {
                    tags.add("thom-ngon");
                }
                cuisine = "Việt Nam";
            } else {
                // 4. MÓN NƯỚC (Bún, Phở, Cháo, Mì nước...)
                // Loại trừ "bánh mì"/"bánh mỳ" khi kiểm tra chữ "mì"/"mỳ" để tránh nhận diện bánh mì là món nước.
                // Thêm "chào" để nhận diện lỗi gõ nhầm của "Chào lòng" (Cháo lòng).
                boolean isSoup = nameLower.contains("bún") || nameLower.contains("phở") || 
                                 nameLower.contains("cháo") || nameLower.contains("chào") || 
                                 nameLower.contains("súp") || nameLower.contains("canh") || nameLower.contains("lẩu") || 
                                 ((nameLower.contains("mì") || nameLower.contains("mỳ")) 
                                  && !nameLower.contains("bánh mì") 
                                  && !nameLower.contains("bánh mỳ") 
                                  && !nameLower.contains("trộn") 
                                  && !nameLower.contains("khô") 
                                  && !nameLower.contains("xào") 
                                  && !nameLower.contains("tương đen"));

                if (isSoup) {
                    tags.add("mon-nuoc");
                    if (nameLower.contains("cháo") || nameLower.contains("chào")) {
                        tags.add("chao");
                        tags.add("de-tieu");
                    } else if (nameLower.contains("bún")) {
                        tags.add("bun");
                    } else if (nameLower.contains("phở")) {
                        tags.add("pho");
                    } else {
                        tags.add("mi-nuoc");
                    }
                    tags.add("ansang");
                    tags.add("nong-hoi");
                    cuisine = "Việt Nam";
                } else {
                    // 5. MÓN KHÔ / CƠM / BÁNH MÌ / HAMBURGER
                    tags.add("mon-kho");
                    if (nameLower.contains("cơm") || nameLower.contains("com")) {
                        tags.add("com");
                        tags.add("com-viet");
                        tags.add("an-trua");
                    } else if (nameLower.contains("bánh mì") || nameLower.contains("bánh mỳ")) {
                        tags.add("banh-mi");
                        tags.add("mon-viet");
                        tags.add("an-sang");
                    } else if (nameLower.contains("hamburger") || nameLower.contains("burger")) {
                        tags.add("hamburger");
                        tags.add("do-an-nhanh");
                        cuisine = "Tây Âu";
                    } else if (nameLower.contains("cơm cuộn") || nameLower.contains("kimbap")) {
                        tags.add("kimbap");
                        tags.add("rong-bien");
                        cuisine = "Hàn Quốc";
                    } else if (nameLower.contains("trộn") || nameLower.contains("tương đen")) {
                        tags.add("mi-tron");
                        tags.add("kho-tron");
                    } else if (nameLower.contains("gỏi cuốn")) {
                        tags.add("goi-cuon");
                        tags.add("khai-vi");
                        tags.add("thanh-mat");
                    } else {
                        tags.add("do-kho");
                    }
                    tags.add("bua-chinh");
                }
            }

            // Spicy & Cuisine specific details
            if (nameLower.contains("cay") || nameLower.contains("lẩu") || nameLower.contains("ớt") || nameLower.contains("kim chi") || nameLower.contains("kimchi")) {
                tags.add("cay");
                spicyLevel = 1;
            } else {
                tags.add("khong-cay");
            }

            if (nameLower.contains("huế") || nameLower.contains("quảng")) {
                cuisine = "Miền Trung";
                tags.add("mien-trung");
            } else if (nameLower.contains("hà nội") || nameLower.contains("bắc") || nameLower.contains("bún chả")) {
                cuisine = "Miền Bắc";
                tags.add("mien-bac");
            } else if (nameLower.contains("sài gòn") || nameLower.contains("nam")) {
                cuisine = "Miền Nam";
                tags.add("mien-nam");
            } else if (nameLower.contains("hàn quốc") || nameLower.contains("tokbokki") || nameLower.contains("kimchi") || nameLower.contains("kimbap")) {
                cuisine = "Hàn Quốc";
                tags.add("han-quoc");
            } else if (nameLower.contains("thái")) {
                cuisine = "Thái Lan";
                tags.add("thai-lan");
            }

            // Protein/ingredient tags
            if (nameLower.contains("gà") || nameLower.contains("ga")) tags.add("ga");
            if (nameLower.contains("bò") || nameLower.contains("bo")) tags.add("bo");
            if (nameLower.contains("heo") || nameLower.contains("thịt") || nameLower.contains("sườn") || nameLower.contains("xá xíu")) tags.add("thit");
            if (nameLower.contains("tôm") || nameLower.contains("cá") || nameLower.contains("hải sản") || nameLower.contains("mực")) tags.add("hai-san");
            if (nameLower.contains("rau") || nameLower.contains("salad")) tags.add("rau");
            if (nameLower.contains("trứng")) tags.add("trung");
            if (nameLower.contains("chay")) tags.add("chay");

            // Final fallback generic tags to ensure at least 5 tags always
            tags.add("binh-dan");
            tags.add("an-trua");

            // Filter unique tags and write back
            List<String> uniqueTags = new ArrayList<>(new LinkedHashSet<>(tags));
            
            // Limit tags to maximum 10
            if (uniqueTags.size() > 10) {
                uniqueTags = uniqueTags.subList(0, 10);
            }

            food.setTags(uniqueTags);
            food.setCuisine(cuisine);
            food.setSpicyLevel(spicyLevel);

            foodRepository.save(food);
            log.info("Applied fallback tags to food {}: {} (Cuisine: {}, Spicy: {})", 
                food.getName(), food.getTags(), food.getCuisine(), food.getSpicyLevel());
        } catch (Exception e) {
            log.error("Failed to apply fallback to food: " + food.getName(), e);
        }
    }

    /**
     * Tạo danh sách Tool declarations cho Gemini Native Function Calling.
     * Các tools này là READ-ONLY, chỉ query dữ liệu.
     */
    public List<GeminiRequest.Tool> createToolsDeclaration() {
        // Tool 1: searchShops
        GeminiRequest.SchemaProperty keywordProp = GeminiRequest.SchemaProperty.builder()
                .type("string").description("Từ khóa tìm quán (VD: trà sữa, bún bò)").build();
        GeminiRequest.SchemaProperty latProp = GeminiRequest.SchemaProperty.builder()
                .type("number").description("Vĩ độ người dùng (optional)").build();
        GeminiRequest.SchemaProperty lngProp = GeminiRequest.SchemaProperty.builder()
                .type("number").description("Kinh độ người dùng (optional)").build();
        GeminiRequest.SchemaProperty radiusProp = GeminiRequest.SchemaProperty.builder()
                .type("number").description("Bán kính tìm kiếm tính bằng km (optional, default 2.0)").build();

        GeminiRequest.FunctionDeclaration searchShopsFn = GeminiRequest.FunctionDeclaration.builder()
                .name("searchShops")
                .description("Tìm kiếm quán ăn đang hoạt động. Có thể tìm theo từ khóa, vị trí gần user, hoặc cả hai.")
                .parameters(GeminiRequest.OpenApiSchema.builder()
                        .type("object")
                        .properties(Map.of(
                                "keyword", keywordProp,
                                "lat", latProp,
                                "lng", lngProp,
                                "radiusKm", radiusProp
                        ))
                        .required(List.of())
                        .build())
                .build();

        // Tool 2: searchFoods
        GeminiRequest.SchemaProperty foodKeywordProp = GeminiRequest.SchemaProperty.builder()
                .type("string").description("Từ khóa tìm món ăn (VD: trà sữa, cơm tấm)").build();
        GeminiRequest.SchemaProperty sortByProp = GeminiRequest.SchemaProperty.builder()
                .type("string").description("Cách sắp xếp: price_asc (rẻ nhất), price_desc (mắc nhất), name (tên A-Z)").build();

        GeminiRequest.FunctionDeclaration searchFoodsFn = GeminiRequest.FunctionDeclaration.builder()
                .name("searchFoods")
                .description("Tìm kiếm món ăn đang bán. Có thể sắp xếp theo giá hoặc tên.")
                .parameters(GeminiRequest.OpenApiSchema.builder()
                        .type("object")
                        .properties(Map.of(
                                "keyword", foodKeywordProp,
                                "sortBy", sortByProp
                        ))
                        .required(List.of())
                        .build())
                .build();

        // Tool 3: getShopDetail
        GeminiRequest.SchemaProperty shopIdProp = GeminiRequest.SchemaProperty.builder()
                .type("string").description("UUID của quán ăn cần xem chi tiết").build();

        GeminiRequest.FunctionDeclaration getShopDetailFn = GeminiRequest.FunctionDeclaration.builder()
                .name("getShopDetail")
                .description("Lấy thông tin chi tiết của một quán ăn bao gồm tên, địa chỉ, giờ mở cửa, menu các món.")
                .parameters(GeminiRequest.OpenApiSchema.builder()
                        .type("object")
                        .properties(Map.of("shopId", shopIdProp))
                        .required(List.of("shopId"))
                        .build())
                .build();

        // Tool 4: getBuildingCoordinates
        GeminiRequest.SchemaProperty buildingNameProp = GeminiRequest.SchemaProperty.builder()
                .type("string").description("Tên tòa nhà KTX (VD: A2, B1, C3, A1)").build();

        GeminiRequest.FunctionDeclaration getBuildingCoordFn = GeminiRequest.FunctionDeclaration.builder()
                .name("getBuildingCoordinates")
                .description("Tra cứu tọa độ (latitude, longitude) của tòa nhà trong khu KTX.")
                .parameters(GeminiRequest.OpenApiSchema.builder()
                        .type("object")
                        .properties(Map.of("name", buildingNameProp))
                        .required(List.of("name"))
                        .build())
                .build();

        GeminiRequest.Tool tool = GeminiRequest.Tool.builder()
                .functionDeclarations(List.of(searchShopsFn, searchFoodsFn, getShopDetailFn, getBuildingCoordFn))
                .build();

        return List.of(tool);
    }

    public List<GeminiRecommendationMatch> recommendFoods(String userQuery, List<Food> availableFoods) {
        if (availableFoods == null || availableFoods.isEmpty()) {
            return Collections.emptyList();
        }

        // Build list of foods to send to Gemini (only send metadata to save token limits)
        List<Map<String, Object>> menuList = new ArrayList<>();
        for (Food f : availableFoods) {
            Map<String, Object> map = new HashMap<>();
            map.put("foodId", f.getId().toString());
            map.put("name", f.getName());
            map.put("description", f.getDescription() != null ? f.getDescription() : "");
            map.put("tags", f.getTags() != null ? f.getTags() : Collections.emptyList());
            map.put("cuisine", f.getCuisine() != null ? f.getCuisine() : "");
            map.put("spicyLevel", f.getSpicyLevel() != null ? f.getSpicyLevel() : 0);
            menuList.add(map);
        }

        try {
            String menuJsonStr = objectMapper.writeValueAsString(menuList);
            String prompt = String.format(
                "Bạn là một trợ lý ảo tư vấn ẩm thực thân thiện tại Việt Nam.\n" +
                "Khách hàng yêu cầu: \"%s\".\n\n" +
                "Dưới đây là danh sách thực đơn các món ăn đang có sẵn của quán:\n" +
                "%s\n\n" +
                "Hãy chọn ra tối đa 3 món phù hợp nhất với yêu cầu trên của khách hàng từ danh sách thực đơn có sẵn ở trên.\n" +
                "Với mỗi món được chọn, hãy giải thích cực kỳ ngắn gọn bằng đúng 1 câu tiếng Việt lý do tại sao món này lại phù hợp.\n" +
                "Định dạng kết quả trả về bắt buộc phải là một mảng JSON các đối tượng có cấu trúc chính xác như sau:\n" +
                "[\n" +
                "  { \"foodId\": \"UUID của món ăn\", \"reason\": \"Lý do gợi ý ngắn gọn bằng tiếng Việt...\" }\n" +
                "]\n" +
                "Chú ý: Nếu không có món nào phù hợp, hãy trả về một mảng trống: []",
                userQuery,
                menuJsonStr
            );

            String responseText = callGeminiApi(GeminiRequest.fromPrompt(prompt, true));
            if (responseText == null || responseText.isBlank()) {
                return Collections.emptyList();
            }

            String cleanedResponse = responseText.trim();
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
            }

            return objectMapper.readValue(cleanedResponse, new TypeReference<List<GeminiRecommendationMatch>>() {});
        } catch (Exception e) {
            log.error("Error getting recommendation matches from Gemini", e);
            return Collections.emptyList();
        }
    }

    private String callGeminiApi(GeminiRequest requestPayload) {
        String url = geminiUrl + "?key=" + apiKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GeminiResponse geminiResponse = response.getBody();
                if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                    GeminiResponse.Candidate candidate = geminiResponse.getCandidates().get(0);
                    if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                        return candidate.getContent().getParts().get(0).getText();
                    }
                }
            }
        } catch (Exception e) {
            log.error("API call to Gemini failed: " + e.getMessage());
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
        throw new RuntimeException("Gemini API returned empty response");
    }

    /**
     * Gọi Gemini API với Function Calling support.
     * Trả về GeminiResponse đầy đủ để controller xử lý agentic loop.
     */
    public GeminiResponse callGeminiWithFunctions(GeminiRequest requestPayload) {
        String url = geminiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Gemini API call with functions failed: " + e.getMessage());
        }
        return null;
    }

    /**
     * Batch re-analyze all foods that have fewer than 5 tags (including null/empty).
     * Processes sequentially with a delay between each call to avoid Gemini API rate limits.
     * @return a summary map with counts of success, failure, and total.
     */
    public Map<String, Object> reanalyzeFoodsWithFewTags() {
        List<Food> foods = foodRepository.findAllWithFewerThanFiveTags();
        log.info("Found {} foods with fewer than 5 tags to re-analyze", foods.size());

        int success = 0;
        int failed = 0;
        List<String> failedNames = new ArrayList<>();

        for (Food food : foods) {
            try {
                log.info("Re-analyzing food [{}/{}]: {} (id={}, currentTags={})", 
                    success + failed + 1, foods.size(), food.getName(), food.getId(),
                    food.getTags() != null ? food.getTags().size() : 0);
                analyzeFoodSync(food.getId());
                success++;
                // Delay 4 seconds between calls to avoid rate limiting
                Thread.sleep(4000);
            } catch (Exception e) {
                failed++;
                failedNames.add(food.getName() + " (" + food.getId() + ")");
                log.error("Failed to re-analyze food: {} (id={})", food.getName(), food.getId(), e);
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", foods.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("failedFoods", failedNames);
        log.info("Batch re-analysis complete: total={}, success={}, failed={}", foods.size(), success, failed);
        return result;
    }

    /**
     * Batch re-analyze all 92 foods in the database with strict logical constraints.
     * Uses a longer delay (4.5 seconds) to avoid RPM rate limits.
     * Fallback covers any transient errors.
     */
    public Map<String, Object> reanalyzeAllFoodsAccurate() {
        List<Food> foods = foodRepository.findAll();
        log.info("Starting accurate re-analysis for all {} foods in the database", foods.size());

        int success = 0;
        int failed = 0;
        List<String> failedNames = new ArrayList<>();

        for (Food food : foods) {
            try {
                log.info("Accurate analysis food [{}/{}]: {} (id={}, currentTags={})", 
                    success + failed + 1, foods.size(), food.getName(), food.getId(),
                    food.getTags() != null ? food.getTags() : "[]");
                
                analyzeFoodSync(food.getId());
                success++;
                // Delay 4.5 seconds to strictly respect 15 RPM
                Thread.sleep(4500);
            } catch (Exception e) {
                failed++;
                failedNames.add(food.getName() + " (" + food.getId() + ")");
                log.error("Failed to accurately analyze food: {} (id={})", food.getName(), food.getId(), e);
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", foods.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("failedFoods", failedNames);
        log.info("Accurate batch re-analysis complete: total={}, success={}, failed={}", foods.size(), success, failed);
        return result;
    }
}
