package com.foodorderingapp.backend.modules.shop.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ShopUpdateRequest {
    @Size(max = 255, message = "Tên quán không được vượt quá 255 ký tự")
    private String name;

    private String address;
    private String description;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

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
}