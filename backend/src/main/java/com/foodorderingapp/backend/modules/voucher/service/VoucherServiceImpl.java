package com.foodorderingapp.backend.modules.voucher.service;

import com.foodorderingapp.backend.core.component.ShopValidationComponent;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.entity.Food;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.Voucher;
import com.foodorderingapp.backend.modules.food.repository.FoodRepository;
import com.foodorderingapp.backend.modules.voucher.dto.request.VoucherCreateRequest;
import com.foodorderingapp.backend.modules.voucher.dto.response.VoucherResponse;
import com.foodorderingapp.backend.modules.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final FoodRepository foodRepository;
    private final ShopValidationComponent shopValidationComponent;

    @Override
    @Transactional
    public VoucherResponse createVoucher(UUID shopId, VoucherCreateRequest request, String vendorPhone) {
        Shop shop = shopValidationComponent.validateAndGetShop(shopId, vendorPhone);

        // Check if voucher code already exists for this shop
        if (voucherRepository.findByShopIdAndCode(shopId, request.getCode().toUpperCase()).isPresent()) {
            throw new AppException("Mã voucher này đã tồn tại ở cửa hàng của bạn!", HttpStatus.BAD_REQUEST);
        }

        List<Food> foods = new ArrayList<>();
        if ("SPECIFIC_FOODS".equals(request.getApplyType()) && request.getFoodIds() != null && !request.getFoodIds().isEmpty()) {
            foods = foodRepository.findAllById(request.getFoodIds());
            for (Food food : foods) {
                if (!food.getShop().getId().equals(shopId)) {
                    throw new AppException("Món ăn với ID " + food.getId() + " không thuộc cửa hàng này!", HttpStatus.BAD_REQUEST);
                }
            }
        }

        Voucher voucher = Voucher.builder()
                .shop(shop)
                .code(request.getCode().toUpperCase())
                .title(request.getTitle())
                .discountType(request.getDiscountType().toUpperCase())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : java.math.BigDecimal.ZERO)
                .maxDiscountValue(request.getMaxDiscountValue())
                .applyType(request.getApplyType().toUpperCase())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .foods(foods)
                .build();

        Voucher savedVoucher = voucherRepository.save(voucher);
        return mapToResponse(savedVoucher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getShopVouchers(UUID shopId, String vendorPhone) {
        shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        return voucherRepository.findAllByShopId(shopId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(UUID shopId, UUID voucherId, String vendorPhone) {
        shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException("Không tìm thấy voucher!", HttpStatus.NOT_FOUND));
        if (!voucher.getShop().getId().equals(shopId)) {
            throw new AppException("Voucher này không thuộc cửa hàng của bạn!", HttpStatus.FORBIDDEN);
        }
        return mapToResponse(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(UUID shopId, UUID voucherId, VoucherCreateRequest request, String vendorPhone) {
        shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException("Không tìm thấy voucher!", HttpStatus.NOT_FOUND));

        if (!voucher.getShop().getId().equals(shopId)) {
            throw new AppException("Voucher này không thuộc cửa hàng của bạn!", HttpStatus.FORBIDDEN);
        }

        // Check code uniqueness if changing
        if (!voucher.getCode().equalsIgnoreCase(request.getCode())) {
            if (voucherRepository.findByShopIdAndCode(shopId, request.getCode().toUpperCase()).isPresent()) {
                throw new AppException("Mã voucher này đã tồn tại ở cửa hàng của bạn!", HttpStatus.BAD_REQUEST);
            }
            voucher.setCode(request.getCode().toUpperCase());
        }

        voucher.setTitle(request.getTitle());
        voucher.setDiscountType(request.getDiscountType().toUpperCase());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMinOrderValue(request.getMinOrderValue() != null ? request.getMinOrderValue() : java.math.BigDecimal.ZERO);
        voucher.setMaxDiscountValue(request.getMaxDiscountValue());
        voucher.setApplyType(request.getApplyType().toUpperCase());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());

        List<Food> foods = new ArrayList<>();
        if ("SPECIFIC_FOODS".equals(request.getApplyType()) && request.getFoodIds() != null && !request.getFoodIds().isEmpty()) {
            foods = foodRepository.findAllById(request.getFoodIds());
            for (Food food : foods) {
                if (!food.getShop().getId().equals(shopId)) {
                    throw new AppException("Món ăn với ID " + food.getId() + " không thuộc cửa hàng này!", HttpStatus.BAD_REQUEST);
                }
            }
        }
        voucher.setFoods(foods);

        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToResponse(updatedVoucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(UUID shopId, UUID voucherId, String vendorPhone) {
        shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException("Không tìm thấy voucher!", HttpStatus.NOT_FOUND));

        if (!voucher.getShop().getId().equals(shopId)) {
            throw new AppException("Voucher này không thuộc cửa hàng của bạn!", HttpStatus.FORBIDDEN);
        }

        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse toggleVoucherStatus(UUID shopId, UUID voucherId, Boolean isActive, String vendorPhone) {
        shopValidationComponent.validateAndGetShop(shopId, vendorPhone);
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new AppException("Không tìm thấy voucher!", HttpStatus.NOT_FOUND));

        if (!voucher.getShop().getId().equals(shopId)) {
            throw new AppException("Voucher này không thuộc cửa hàng của bạn!", HttpStatus.FORBIDDEN);
        }

        voucher.setIsActive(isActive);
        Voucher updatedVoucher = voucherRepository.save(voucher);
        return mapToResponse(updatedVoucher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getStudentActiveVouchers(UUID shopId) {
        return voucherRepository.findActiveVouchers(shopId, LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        List<UUID> foodIds = null;
        if (voucher.getFoods() != null) {
            foodIds = voucher.getFoods().stream().map(Food::getId).collect(Collectors.toList());
        }

        String displayDiscountType = "";
        if (voucher.getDiscountType() != null) {
            displayDiscountType = "PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType()) ? "Theo phần trăm" : "Số tiền cố định";
        }

        String displayApplyType = "";
        if (voucher.getApplyType() != null) {
            displayApplyType = "ALL_MENU".equalsIgnoreCase(voucher.getApplyType()) ? "Toàn bộ thực đơn" : "Món ăn cụ thể";
        }

        return VoucherResponse.builder()
                .id(voucher.getId())
                .shopId(voucher.getShop().getId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .discountType(voucher.getDiscountType())
                .displayDiscountType(displayDiscountType)
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscountValue(voucher.getMaxDiscountValue())
                .applyType(voucher.getApplyType())
                .displayApplyType(displayApplyType)
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .isActive(voucher.getIsActive())
                .foodIds(foodIds)
                .build();
    }
}
