package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.LoginRequest;
import com.foodorderingapp.backend.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.dto.request.VerifyOtpRequest;
import com.foodorderingapp.backend.dto.response.AuthResponse;
import com.foodorderingapp.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<AuthResponse> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<AuthResponse> registerVendor(@Valid @RequestBody VendorRegisterRequest request) {
        return ResponseEntity.ok(authService.registerVendor(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

}
