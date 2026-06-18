package com.foodorderingapp.backend.modules.voucher.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private UUID id;
    private UUID shopId;
    private String code;
    private String title;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscountValue;
    private String applyType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private List<UUID> foodIds;
}
