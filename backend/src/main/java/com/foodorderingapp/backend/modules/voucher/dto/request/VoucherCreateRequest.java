package com.foodorderingapp.backend.modules.voucher.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class VoucherCreateRequest {

    @NotBlank(message = "Mã voucher không được để trống")
    private String code;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Loại giảm giá không được để trống")
    private String discountType; // 'PERCENTAGE', 'FIXED_AMOUNT'

    @NotNull(message = "Giá trị giảm giá không được để trống")
    private BigDecimal discountValue;

    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;

    @NotBlank(message = "Loại áp dụng không được để trống")
    private String applyType; // 'ALL_MENU', 'SPECIFIC_FOODS'

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private List<UUID> foodIds; // List of applicable food IDs
}
