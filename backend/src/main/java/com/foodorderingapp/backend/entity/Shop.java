package com.foodorderingapp.backend.entity;

import com.foodorderingapp.backend.core.enums.ShopStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "shops",
        indexes = {
                @Index(name = "idx_shop_owner", columnList = "owner_id"),
                @Index(name = "idx_shop_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private ShopStatus status = ShopStatus.PENDING;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "logo_url")
    private String logoUrl;

    @Builder.Default
    @Column(name = "is_open")
    private Boolean isOpen = true;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_owner")
    private String bankAccountOwner;

    @Builder.Default
    @Column(name = "order_alerts_enabled")
    private Boolean orderAlertsEnabled = true;

    @Builder.Default
    @Column(name = "dorm_promotions_enabled")
    private Boolean dormPromotionsEnabled = true;

    @Builder.Default
    @Column(name = "turbo_mode_enabled")
    private Boolean turboModeEnabled = false;

    @Column(name = "mon_fri_open_time")
    private LocalTime monFriOpenTime;

    @Column(name = "mon_fri_close_time")
    private LocalTime monFriCloseTime;

    @Column(name = "sat_open_time")
    private LocalTime satOpenTime;

    @Column(name = "sat_close_time")
    private LocalTime satCloseTime;

    @Column(name = "sun_open_time")
    private LocalTime sunOpenTime;

    @Column(name = "sun_close_time")
    private LocalTime sunCloseTime;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Food> foods;
}