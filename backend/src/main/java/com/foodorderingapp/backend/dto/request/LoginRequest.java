package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Phone number cannot be left blank")
    private String phone;

    @NotBlank(message = "Password cannot be left blank")
    private String password;
}