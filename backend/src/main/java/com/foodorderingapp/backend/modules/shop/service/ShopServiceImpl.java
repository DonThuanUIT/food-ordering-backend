package com.foodorderingapp.backend.modules.shop.service;

import com.foodorderingapp.backend.core.component.ShopValidationComponent;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopCreateRequest;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopCloseRequest;
import com.foodorderingapp.backend.modules.shop.dto.request.ShopStatusRequest;
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
import com.foodorderingapp.backend.core.util.ShopOpeningHours;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.email.EmailService;
import com.foodorderingapp.backend.modules.email.OtpService;
import com.foodorderingapp.backend.modules.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

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
                .displayStatusText(calculateVerificationStatusText(shop.getStatus()))
                .isActive(shop.getIsActive())
                .isOpen(shop.getIsOpen())
                .displayStatus(calculateDisplayStatus(shop))
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude());

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
            shopPage = shopRepository.findVisibleStudentShops(ShopStatus.APPROVED, pageable);
        }
        List<ShopResponse> content = new ArrayList<>(shopPage.getContent().stream()
                .map(this::mapToStudentResponse)
                .toList());
        content.sort((s1, s2) -> {
            if (s1.getDisplayStatus().equals(s2.getDisplayStatus())) return 0;
            return s1.getDisplayStatus().equals("ĐANG HOẠT ĐỘNG") ? -1 : 1;
        });
        return new PageImpl<>(content, pageable, shopPage.getTotalElements());

    }

    private ShopResponse mapToStudentResponse(Shop shop) {
        String coverUrl = null;
        String logoUrl = null;
        
        ShopSettings settings = shop.getSettings();
        if (settings != null) {
            coverUrl = settings.getCoverUrl();
            logoUrl = settings.getLogoUrl();
        }
        LocalTime[] hours = ShopOpeningHours.effectiveOpeningHoursToday(shop);
        
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .description(shop.getDescription())
                .address(shop.getAddress())
                .openTime(hours[0])
                .closeTime(hours[1])
                .status(shop.getStatus().name())
                .displayStatusText(calculateVerificationStatusText(shop.getStatus()))
                .isActive(shop.getIsActive())
                .displayStatus(calculateDisplayStatus(shop))
                .coverUrl(coverUrl)
                .logoUrl(logoUrl)
                .isOpen(shop.getIsOpen())
                .latitude(shop.getLatitude())
                .longitude(shop.getLongitude())
                .build();
    }

    private String calculateDisplayStatus(Shop shop) {
        String displayStatus = "ĐÓNG CỬA";
        if (Boolean.TRUE.equals(shop.getIsOpen()) && ShopOpeningHours.isOpenNow(shop)) {
            displayStatus = "ĐANG HOẠT ĐỘNG";
        }
        return displayStatus;
    }

    @Override
    @Transactional(readOnly = true)
    public ShopDetailResponse getShopDetailWithMenu(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Khong tim thay cua hang voi ID: " + shopId, HttpStatus.NOT_FOUND));

        if (shop.getStatus() != ShopStatus.APPROVED
                || !Boolean.TRUE.equals(shop.getIsActive())
                || isShopOwnerLocked(shop)) {
            throw new AppException("Quán ăn hiện không khả dụng", HttpStatus.NOT_FOUND);
        }

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
                                    .categoryId(f.getCategory().getId())
                                    .categoryName(f.getCategory().getName())
                                    .tags(f.getTags())
                                    .cuisine(f.getCuisine())
                                    .spicyLevel(f.getSpicyLevel())
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
        LocalTime[] hours = ShopOpeningHours.effectiveOpeningHoursToday(shop);
        return new ShopDetailResponse(
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                shop.getDescription(),
                coverUrl,
                logoUrl,
                hours[0],
                hours[1],
                shop.getIsOpen(),
                shop.getLatitude(),
                shop.getLongitude(),
                menu
        );
    }

    @Override
    @Transactional
    public ShopResponse updateShopProfile(UUID shopId, ShopUpdateRequest request, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        if (shop.getStatus() == ShopStatus.CLOSED) {
            throw new AppException("Cửa hàng này đã bị đóng vĩnh viễn và không thể chỉnh sửa thông tin!", HttpStatus.BAD_REQUEST);
        }

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
        if (request.getLatitude() != null) {
            shop.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            shop.setLongitude(request.getLongitude());
        }
        if (request.getDescription() != null) {
            shop.setDescription(request.getDescription());
        }
        boolean hasDaySpecificHours = request.getMonFriOpenTime() != null
                || request.getMonFriCloseTime() != null
                || request.getSatOpenTime() != null
                || request.getSatCloseTime() != null
                || request.getSunOpenTime() != null
                || request.getSunCloseTime() != null;
        if (request.getOpenTime() != null) {
            shop.setOpenTime(request.getOpenTime());
        }
        if (request.getCloseTime() != null) {
            shop.setCloseTime(request.getCloseTime());
        }
        if (!hasDaySpecificHours && request.getOpenTime() != null && request.getCloseTime() != null) {
            settings.setMonFriOpenTime(request.getOpenTime());
            settings.setMonFriCloseTime(request.getCloseTime());
            settings.setSatOpenTime(request.getOpenTime());
            settings.setSatCloseTime(request.getCloseTime());
            settings.setSunOpenTime(request.getOpenTime());
            settings.setSunCloseTime(request.getCloseTime());
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

    private void applyAdminStatusAvailability(Shop shop, ShopStatus targetStatus) {
        if (targetStatus == ShopStatus.APPROVED) {
            shop.setIsActive(true);
            if (shop.getIsOpen() == null) {
                shop.setIsOpen(true);
            }
            return;
        }

        if (targetStatus == ShopStatus.REJECTED || targetStatus == ShopStatus.BANNED) {
            shop.setIsActive(false);
            shop.setIsOpen(false);
            ShopSettings settings = shop.getSettings();
            if (settings != null) {
                settings.setIsOpen(false);
            }
        }
    }

    @Override
    @Transactional
    public ShopResponse toggleShopStatus(UUID shopId, Map<String, Boolean> statusMap, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        if (shop.getStatus() == ShopStatus.CLOSED) {
            throw new AppException("Cửa hàng này đã bị đóng vĩnh viễn và không thể bật/tắt trạng thái!", HttpStatus.BAD_REQUEST);
        }
        if (shop.getStatus() == ShopStatus.BANNED || shop.getStatus() == ShopStatus.REJECTED) {
            throw new AppException("Cửa hàng hiện không được phép hoạt động", HttpStatus.BAD_REQUEST);
        }
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
    public ShopResponse updateShopStatus(UUID shopId, com.foodorderingapp.backend.modules.shop.dto.request.ShopStatusRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Không tìm thấy quán ăn!", HttpStatus.NOT_FOUND));

        if (shop.getStatus() == ShopStatus.CLOSED && "REJECTED".equals(request.status())) {
            throw new AppException("Thao tác không hợp lệ. Cửa hàng này đã được xử lý duyệt hoặc từ chối trước đó!", HttpStatus.BAD_REQUEST);
        }

        ShopStatus currentStatus = shop.getStatus();
        ShopStatus targetStatus = ShopStatus.valueOf(request.status());
        if (currentStatus == ShopStatus.CLOSED) {
            throw new AppException("KhÃ´ng thá»ƒ cáº­p nháº­t cá»­a hÃ ng Ä‘Ã£ Ä‘Ã³ng vÄ©nh viá»…n", HttpStatus.BAD_REQUEST);
        }
        if (currentStatus == targetStatus) {
            applyAdminStatusAvailability(shop, targetStatus);
            return mapToResponse(shopRepository.save(shop));
        }

        shop.setStatus(targetStatus);
        applyAdminStatusAvailability(shop, targetStatus);
        Shop updatedShop = shopRepository.save(shop);

        String vendorEmail = shop.getOwner().getEmail();
        if (currentStatus == ShopStatus.PENDING && targetStatus != ShopStatus.BANNED) {
            emailService.sendShopStatusHtmlEmail(vendorEmail, shop.getName(), targetStatus == ShopStatus.APPROVED);
        }

        log.info("Admin vừa cập nhật trạng thái quán {} sang {}", shopId, targetStatus);
        return mapToResponse(updatedShop);
    }
    @Override
    @Transactional(readOnly = true)
    public ShopResponse getShopDetailForAdmin(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Không tìm thấy quán ăn với ID: " + shopId, HttpStatus.NOT_FOUND));

        return this.mapToResponse(shop);
    }

    @Override
    @Transactional
    public void requestCloseShopOtp(UUID shopId, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        if (shop.getStatus() == ShopStatus.CLOSED) {
            throw new AppException("Cửa hàng này đã được đóng vĩnh viễn!", HttpStatus.BAD_REQUEST);
        }
        User owner = shop.getOwner();

        // Generate OTP and save to Redis
        String otpCode = otpService.generateAndSaveOtp(owner.getEmail());

        // Send email with OTP
        emailService.sendCloseShopOtpEmail(owner.getEmail(), shop.getName(), otpCode);
    }

    @Override
    @Transactional
    public void confirmCloseShop(UUID shopId, ShopCloseRequest request, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        if (shop.getStatus() == ShopStatus.CLOSED) {
            throw new AppException("Cửa hàng này đã được đóng vĩnh viễn từ trước!", HttpStatus.BAD_REQUEST);
        }
        User owner = shop.getOwner();

        if ("PASSWORD".equalsIgnoreCase(request.getVerificationType())) {
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new AppException("Mật khẩu xác thực không được để trống!", HttpStatus.BAD_REQUEST);
            }
            if (!passwordEncoder.matches(request.getPassword(), owner.getPassword())) {
                throw new AppException("Mật khẩu xác thực không chính xác!", HttpStatus.UNAUTHORIZED);
            }
        } else if ("OTP".equalsIgnoreCase(request.getVerificationType())) {
            if (request.getOtpCode() == null || request.getOtpCode().isBlank()) {
                throw new AppException("Mã OTP không được để trống!", HttpStatus.BAD_REQUEST);
            }
            boolean isValid = otpService.validateOtp(owner.getEmail(), request.getOtpCode());
            if (!isValid) {
                throw new AppException("Mã OTP không hợp lệ hoặc đã hết hạn!", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new AppException("Phương thức xác thực không hợp lệ!", HttpStatus.BAD_REQUEST);
        }

        // Close shop permanently
        shop.setStatus(ShopStatus.CLOSED);
        shop.setIsActive(false);
        shop.setIsOpen(false);
        if (shop.getSettings() != null) {
            shop.getSettings().setIsOpen(false);
        }

        shopRepository.save(shop);
        log.info("Cửa hàng {} ({}) đã đóng vĩnh viễn thành công bởi vendor {}", shop.getName(), shop.getId(), vendorPhone);
    }

    private boolean isShopOwnerLocked(Shop shop) {
        return shop.getOwner() != null && Boolean.TRUE.equals(shop.getOwner().getIsLocked());
    }

    private String calculateVerificationStatusText(ShopStatus status) {
        if (status == null) return "";
        return switch (status) {
            case PENDING -> "ĐANG CHỜ DUYỆT";
            case APPROVED -> "ĐÃ PHÊ DUYỆT";
            case REJECTED -> "ĐÃ BỊ TỪ CHỐI";
            case BANNED -> "ĐÃ BỊ KHÓA";
            case CLOSED -> "ĐÃ ĐÓNG CỬA VĨNH VIỄN";
        };
    }
}
