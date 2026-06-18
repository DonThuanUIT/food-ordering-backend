package com.foodorderingapp.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "shop_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopSettings {

    @Id
    @Column(name = "shop_id")
    private UUID shopId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "shop_id")
    @JsonIgnore
    private Shop shop;

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
}
