package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Email cannot be left blank")
    private String email;

    @NotBlank(message = "OTP cannot be left blank")
    private String otpCode;
}
