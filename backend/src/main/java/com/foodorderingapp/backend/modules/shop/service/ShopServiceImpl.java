package com.foodorderingapp.backend.modules.shop.service;

import com.foodorderingapp.backend.core.component.ShopValidationComponent;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopUpdateRequest;
import com.foodorderingapp.backend.modules.food.dto.response.CategoryMenuResponse;
import com.foodorderingapp.backend.modules.food.dto.response.FoodResponse;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopDetailResponse;
import com.foodorderingapp.backend.modules.shop.dto.response.ShopResponse;
import com.foodorderingapp.backend.entity.Category;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.email.EmailService;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final ShopValidationComponent shopValidationComponent;
    private final EmailService emailService;

    @Override
    @Transactional
    public ShopResponse createShop(ShopCreateRequest request, String ownerPhone) {
        User owner = userRepository.findByPhone(ownerPhone)
                .orElseThrow(() -> new RuntimeException("No owner information found"));

        if (shopRepository.existsByNameAndOwnerId(request.getName(), owner.getId())) {
            throw new RuntimeException("There is already a shop with this name");
        }

        Shop newShop = Shop.builder()
                .owner(owner)
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .status(ShopStatus.PENDING)
                .isActive(true)
                .build();

        Shop savedShop = shopRepository.save(newShop);
        log.info("{} vendor has just created a new shop : {}", ownerPhone, savedShop.getName());

        return mapToResponse(savedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopResponse> getVendorShops(String ownerPhone) {
        User owner = userRepository.findByPhone(ownerPhone)
                .orElseThrow(() -> new RuntimeException("No one owner information found"));

        List<Shop> shops = shopRepository.findAllByOwnerId(owner.getId());

        return shops.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ShopResponse mapToResponse(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .address(shop.getAddress())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .status(shop.getStatus().name())
                .isActive(shop.getIsActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getAllShops(Pageable pageable, String keyword) {

        Page<Shop> shopPage;
        if(keyword != null && !keyword.trim().isEmpty()){
            shopPage = shopRepository.searchShops(keyword, ShopStatus.APPROVED, pageable);
        } else {
            shopPage = shopRepository.findAllByStatusAndIsActiveTrue(ShopStatus.APPROVED, pageable);
        }
        List<ShopResponse> content = new ArrayList<>(shopPage.getContent().stream()
                .map(this::mapToStudentResponse)
                .toList());
        content.sort((s1, s2) -> {
            if (s1.getDisplayStatus().equals(s2.getDisplayStatus())) return 0;
            return s1.getDisplayStatus().equals("OPENING") ? -1 : 1;
        });
        return new PageImpl<>(content, pageable, shopPage.getTotalElements());

    }

    private ShopResponse mapToStudentResponse(Shop shop) {
        LocalTime now = LocalTime.now();
        String displayStatus = "CLOSED";
        if (shop.getOpenTime() != null && shop.getCloseTime() != null) {
            if (shop.getCloseTime().isAfter(shop.getOpenTime())) {
                if (now.isAfter(shop.getOpenTime()) && now.isBefore(shop.getCloseTime())) {
                    displayStatus = "OPENING";
                }
            }
        } else {
            if(now.isAfter(shop.getOpenTime()) || now.isBefore(shop.getCloseTime())) {
                displayStatus = "OPENING";
            }
        }
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .status(shop.getStatus().name())
                .isActive(shop.getIsActive())
                .displayStatus(displayStatus)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ShopDetailResponse getShopDetailWithMenu(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Khong tim thay cua hang voi ID: " + shopId, HttpStatus.NOT_FOUND));

        List<Food> foods = foodRepository.findAllByShopIdWithCategory(shopId);
        Map<Category, List<Food>> grouped = foods.stream().collect(Collectors.groupingBy(Food::getCategory));
        List<CategoryMenuResponse> menu = grouped.entrySet().stream()
                .map(entry -> {
                    Category cat = entry.getKey(); // Lấy thông tin Category
                    List<FoodResponse> foodResponses = entry.getValue().stream()
                            .map(f -> FoodResponse.builder()
                                    .id(f.getId())
                                    .name(f.getName())
                                    .price(f.getPrice())
                                    .isAvailable(f.getIsAvailable())
                                    .imageUrl(f.getImageUrl())
                                    .description(f.getDescription())
                                    .build()
                            ).toList();
                    return new CategoryMenuResponse(cat.getId(), cat.getName(), foodResponses);
                }).toList();
        return new ShopDetailResponse(
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                shop.getDescription(),
                menu
        );
    }

    @Override
    @Transactional
    public ShopResponse updateShopProfile(UUID shopId, ShopUpdateRequest request, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);

        if (!shop.getName().equals(request.getName()) &&
                shopRepository.existsByNameAndOwnerId(request.getName(), shop.getOwner().getId())) {
            throw new AppException("Bạn đã có một quán ăn khác mang tên này!", HttpStatus.BAD_REQUEST);
        }

        shop.setName(request.getName());
        shop.setAddress(request.getAddress());
        shop.setDescription(request.getDescription());
        shop.setOpenTime(request.getOpenTime());
        shop.setCloseTime(request.getCloseTime());

        Shop updatedShop = shopRepository.save(shop);
        log.info("Vendor {} vừa cập nhật thông tin quán: {}", vendorPhone, updatedShop.getId());

        return mapToResponse(updatedShop);
    }

    @Override
    @Transactional
    public ShopResponse toggleShopStatus(UUID shopId, Boolean isActive, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        shop.setIsActive(isActive);
        Shop updatedShop = shopRepository.save(shop);
        log.info("Vendor {} đã thay đổi trạng thái quán {} thành: {}", vendorPhone, shopId, isActive);

        return mapToResponse(updatedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopResponse> getShopsForAdmin(ShopStatus status, Pageable pageable) {
        // Sử dụng hàm findAllByStatus chúng ta vừa thêm vào Repository ở Bước 2
        return shopRepository.findAllByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ShopResponse updateShopStatus(UUID shopId, ShopStatus status) {
        // 1. Tìm quán ăn
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Không tìm thấy quán ăn!", HttpStatus.NOT_FOUND));

        // 2. Cập nhật trạng thái mới
        shop.setStatus(status);
        Shop updatedShop = shopRepository.save(shop);

        // 3. [BE-Task 3] Gửi email thông báo kết quả cho Vendor
        String vendorEmail = shop.getOwner().getEmail();
        String subject = "Thông báo kết quả duyệt quán ăn: " + shop.getName();
        String message = status == ShopStatus.APPROVED
                ? "Chúc mừng! Quán ăn của bạn đã được duyệt. Bây giờ bạn có thể bắt đầu kinh doanh."
                : "Rất tiếc, yêu cầu đăng ký quán ăn của bạn đã bị từ chối. Vui lòng liên hệ Admin để biết thêm chi tiết.";

        emailService.sendShopStatusHtmlEmail(vendorEmail, shop.getName(), status == ShopStatus.APPROVED);

        log.info("Admin vừa cập nhật trạng thái quán {} sang {}", shopId, status);
        return mapToResponse(updatedShop);
    }
}