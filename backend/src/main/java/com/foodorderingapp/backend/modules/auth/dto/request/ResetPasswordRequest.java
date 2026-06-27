package com.foodorderingapp.backend.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format!")
    private String email;

    @NotBlank(message = "OTP code cannot be blank")
    private String otpCode;

    @NotBlank(message = "New password cannot be blank")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$", 
             message = "Mật khẩu ít nhất 8 ký tự, gồm hoa, thường, số và ký tự đặc biệt")
    private String newPassword;
}
