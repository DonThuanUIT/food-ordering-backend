package com.foodorderingapp.backend.modules.shop.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {
    private UUID id;
    private String name;
    private String description;
    private String address;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
    private String status;
    private String displayStatusText;
    private boolean isActive;
    private String displayStatus;

    private String coverUrl;
    private String logoUrl;
    private Boolean isOpen;
    private String email;
    private String phone;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountOwner;
    private Boolean orderAlertsEnabled;
    private Boolean dormPromotionsEnabled;
    private Boolean turboModeEnabled;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime monFriOpenTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime monFriCloseTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime satOpenTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime satCloseTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sunOpenTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime sunCloseTime;

    private Double latitude;
    private Double longitude;
}
