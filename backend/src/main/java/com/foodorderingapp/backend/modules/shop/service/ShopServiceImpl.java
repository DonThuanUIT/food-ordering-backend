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
import com.foodorderingapp.backend.entity.ShopSettings;
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
                .isOpen(true)
                .build();

        ShopSettings settings = ShopSettings.builder()
                .shop(newShop)
                .coverUrl("")
                .logoUrl("")
                .isOpen(true)
                .orderAlertsEnabled(true)
                .dormPromotionsEnabled(true)
                .turboModeEnabled(false)
                .monFriOpenTime(request.getOpenTime())
                .monFriCloseTime(request.getCloseTime())
                .satOpenTime(request.getOpenTime())
                .satCloseTime(request.getCloseTime())
                .sunOpenTime(request.getOpenTime())
                .sunCloseTime(request.getCloseTime())
                .build();
        newShop.setSettings(settings);

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
        ShopResponse.ShopResponseBuilder builder = ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .address(shop.getAddress())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .status(shop.getStatus().name())
                .isActive(shop.getIsActive())
                .isOpen(shop.getIsOpen());

        ShopSettings settings = shop.getSettings();
        if (settings != null) {
            builder.coverUrl(settings.getCoverUrl())
                    .logoUrl(settings.getLogoUrl())
                    .email(settings.getEmail())
                    .phone(settings.getPhone())
                    .bankName(settings.getBankName())
                    .bankAccountNumber(settings.getBankAccountNumber())
                    .bankAccountOwner(settings.getBankAccountOwner())
                    .orderAlertsEnabled(settings.getOrderAlertsEnabled())
                    .dormPromotionsEnabled(settings.getDormPromotionsEnabled())
                    .turboModeEnabled(settings.getTurboModeEnabled())
                    .monFriOpenTime(settings.getMonFriOpenTime())
                    .monFriCloseTime(settings.getMonFriCloseTime())
                    .satOpenTime(settings.getSatOpenTime())
                    .satCloseTime(settings.getSatCloseTime())
                    .sunOpenTime(settings.getSunOpenTime())
                    .sunCloseTime(settings.getSunCloseTime());
        }

        return builder.build();
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
        String coverUrl = null;
        String logoUrl = null;
        
        ShopSettings settings = shop.getSettings();
        if (Boolean.TRUE.equals(shop.getIsOpen())) {
            LocalTime open = shop.getOpenTime();
            LocalTime close = shop.getCloseTime();
            
            if (settings != null) {
                java.time.DayOfWeek day = java.time.LocalDate.now().getDayOfWeek();
                if (day == java.time.DayOfWeek.SATURDAY && settings.getSatOpenTime() != null && settings.getSatCloseTime() != null) {
                    open = settings.getSatOpenTime();
                    close = settings.getSatCloseTime();
                } else if (day == java.time.DayOfWeek.SUNDAY && settings.getSunOpenTime() != null && settings.getSunCloseTime() != null) {
                    open = settings.getSunOpenTime();
                    close = settings.getSunCloseTime();
                } else if (settings.getMonFriOpenTime() != null && settings.getMonFriCloseTime() != null) {
                    open = settings.getMonFriOpenTime();
                    close = settings.getMonFriCloseTime();
                }
            }

            if (open != null && close != null) {
                if (close.isAfter(open)) {
                    if (now.isAfter(open) && now.isBefore(close)) {
                        displayStatus = "OPENING";
                    }
                } else {
                    if (now.isAfter(open) || now.isBefore(close)) {
                        displayStatus = "OPENING";
                    }
                }
            }
        }
        
        if (settings != null) {
            coverUrl = settings.getCoverUrl();
            logoUrl = settings.getLogoUrl();
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
                .coverUrl(coverUrl)
                .logoUrl(logoUrl)
                .isOpen(shop.getIsOpen())
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
        String coverUrl = null;
        String logoUrl = null;
        ShopSettings settings = shop.getSettings();
        if (settings != null) {
            coverUrl = settings.getCoverUrl();
            logoUrl = settings.getLogoUrl();
        }
        return new ShopDetailResponse(
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                shop.getDescription(),
                coverUrl,
                logoUrl,
                shop.getIsOpen(),
                menu
        );
    }

    @Override
    @Transactional
    public ShopResponse updateShopProfile(UUID shopId, ShopUpdateRequest request, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);

        ShopSettings settings = shop.getSettings();
        if (settings == null) {
            settings = ShopSettings.builder()
                    .shop(shop)
                    .coverUrl("")
                    .logoUrl("")
                    .isOpen(true)
                    .orderAlertsEnabled(true)
                    .dormPromotionsEnabled(true)
                    .turboModeEnabled(false)
                    .monFriOpenTime(shop.getOpenTime())
                    .monFriCloseTime(shop.getCloseTime())
                    .satOpenTime(shop.getOpenTime())
                    .satCloseTime(shop.getCloseTime())
                    .sunOpenTime(shop.getOpenTime())
                    .sunCloseTime(shop.getCloseTime())
                    .build();
            shop.setSettings(settings);
        }

        if (request.getName() != null) {
            if (!shop.getName().equals(request.getName()) &&
                    shopRepository.existsByNameAndOwnerId(request.getName(), shop.getOwner().getId())) {
                throw new AppException("Bạn đã có một quán ăn khác mang tên này!", HttpStatus.BAD_REQUEST);
            }
            shop.setName(request.getName());
        }
        if (request.getAddress() != null) {
            shop.setAddress(request.getAddress());
        }
        if (request.getDescription() != null) {
            shop.setDescription(request.getDescription());
        }
        if (request.getOpenTime() != null) {
            shop.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            shop.setCloseTime(request.getCloseTime());
        }
        if (request.getCoverUrl() != null) {
            settings.setCoverUrl(request.getCoverUrl());
        }
        if (request.getLogoUrl() != null) {
            settings.setLogoUrl(request.getLogoUrl());
        }
        if (request.getIsOpen() != null) {
            shop.setIsOpen(request.getIsOpen());
            settings.setIsOpen(request.getIsOpen());
        }
        if (request.getEmail() != null) {
            settings.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            settings.setPhone(request.getPhone());
        }
        if (request.getBankName() != null) {
            settings.setBankName(request.getBankName());
        }
        if (request.getBankAccountNumber() != null) {
            settings.setBankAccountNumber(request.getBankAccountNumber());
        }
        if (request.getBankAccountOwner() != null) {
            settings.setBankAccountOwner(request.getBankAccountOwner());
        }
        if (request.getOrderAlertsEnabled() != null) {
            settings.setOrderAlertsEnabled(request.getOrderAlertsEnabled());
        }
        if (request.getDormPromotionsEnabled() != null) {
            settings.setDormPromotionsEnabled(request.getDormPromotionsEnabled());
        }
        if (request.getTurboModeEnabled() != null) {
            settings.setTurboModeEnabled(request.getTurboModeEnabled());
        }
        if (request.getMonFriOpenTime() != null) {
            settings.setMonFriOpenTime(request.getMonFriOpenTime());
        }
        if (request.getMonFriCloseTime() != null) {
            settings.setMonFriCloseTime(request.getMonFriCloseTime());
        }
        if (request.getSatOpenTime() != null) {
            settings.setSatOpenTime(request.getSatOpenTime());
        }
        if (request.getSatCloseTime() != null) {
            settings.setSatCloseTime(request.getSatCloseTime());
        }
        if (request.getSunOpenTime() != null) {
            settings.setSunOpenTime(request.getSunOpenTime());
        }
        if (request.getSunCloseTime() != null) {
            settings.setSunCloseTime(request.getSunCloseTime());
        }

        Shop updatedShop = shopRepository.save(shop);
        log.info("Vendor {} vừa cập nhật thông tin quán: {}", vendorPhone, updatedShop.getId());

        return mapToResponse(updatedShop);
    }

    @Override
    @Transactional
    public ShopResponse toggleShopStatus(UUID shopId, Map<String, Boolean> statusMap, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        if (statusMap.containsKey("isActive")) {
            shop.setIsActive(statusMap.get("isActive"));
        }
        if (statusMap.containsKey("isOpen")) {
            shop.setIsOpen(statusMap.get("isOpen"));
            ShopSettings settings = shop.getSettings();
            if (settings != null) {
                settings.setIsOpen(statusMap.get("isOpen"));
            }
        }
        Shop updatedShop = shopRepository.save(shop);
        log.info("Vendor {} đã thay đổi trạng thái quán {} với map: {}", vendorPhone, shopId, statusMap);

        return mapToResponse(updatedShop);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopResponse getVendorShopById(UUID shopId, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        return mapToResponse(shop);
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
    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopDetailForAdmin(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Không tìm thấy quán ăn với ID: " + shopId, HttpStatus.NOT_FOUND));

        return this.mapToResponse(shop);
    }
}