package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.dto.request.LoginRequest;
import com.foodorderingapp.backend.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.dto.request.VerifyOtpRequest;
import com.foodorderingapp.backend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerStudent(StudentRegisterRequest request);
    AuthResponse registerVendor(VendorRegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
}