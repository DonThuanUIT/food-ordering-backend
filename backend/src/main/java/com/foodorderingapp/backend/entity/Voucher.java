package com.foodorderingapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(name = "discount_type", nullable = false, length = 50)
    private String discountType; // 'PERCENTAGE', 'FIXED_AMOUNT'

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Builder.Default
    @Column(name = "min_order_value")
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount_value")
    private BigDecimal maxDiscountValue;

    @Column(name = "apply_type", nullable = false, length = 50)
    private String applyType; // 'ALL_MENU', 'SPECIFIC_FOODS'

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "voucher_foods",
            joinColumns = @JoinColumn(name = "voucher_id"),
            inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<Food> foods;
}
