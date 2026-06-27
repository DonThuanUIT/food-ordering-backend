package com.foodorderingapp.backend.modules.auth;

import com.foodorderingapp.backend.modules.auth.dto.request.LoginRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.ResendOtpRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.BaseRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.VerifyOtpRequest;
import com.foodorderingapp.backend.modules.auth.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerStudent(StudentRegisterRequest request);
    AuthResponse registerVendor(VendorRegisterRequest request);
    AuthResponse registerShipper(BaseRegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse resendOtp(ResendOtpRequest request);
    AuthResponse sendForgotPasswordOtp(String email);
    AuthResponse resetPassword(com.foodorderingapp.backend.modules.auth.dto.request.ResetPasswordRequest request);
}