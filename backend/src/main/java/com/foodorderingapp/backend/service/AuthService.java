package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.*;
import com.foodorderingapp.backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerStudent(StudentRegisterRequest request);
    AuthResponse registerVendor(VendorRegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse resendOtp(ResendOtpRequest request);
}