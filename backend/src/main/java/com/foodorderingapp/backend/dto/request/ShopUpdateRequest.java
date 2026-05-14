package com.foodorderingapp.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ShopUpdateRequest {
    @NotBlank(message = "Tên quán không được để trống")
    @Size(max = 255, message = "Tên quán không được vượt quá 255 ký tự")
    private String name;

    private String address;
    private String description;

    @NotNull(message = "Giờ mở cửa không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "Giờ đóng cửa không được để trống")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
}