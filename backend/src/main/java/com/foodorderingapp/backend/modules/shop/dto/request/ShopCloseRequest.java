package com.foodorderingapp.backend.modules.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopCloseRequest {
    @NotBlank(message = "Phương thức xác thực (PASSWORD hoặc OTP) không được để trống")
    private String verificationType; // "PASSWORD" hoặc "OTP"
    
    private String password;
    
    private String otpCode;
}
