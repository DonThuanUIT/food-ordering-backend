package com.foodorderingapp.backend.modules.auth;

import com.foodorderingapp.backend.modules.auth.dto.request.BaseRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.LoginRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.StudentRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.VendorRegisterRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.VerifyOtpRequest;
import com.foodorderingapp.backend.modules.auth.dto.request.ResendOtpRequest;
import com.foodorderingapp.backend.modules.auth.dto.response.AuthResponse;
import com.foodorderingapp.backend.modules.auth.AuthService;
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

    @PostMapping("/register/shipper")
    public ResponseEntity<AuthResponse> registerShipper(@Valid @RequestBody BaseRegisterRequest request) {
        return ResponseEntity.ok(authService.registerShipper(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<AuthResponse> sendForgotPasswordOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendForgotPasswordOtp(email));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody com.foodorderingapp.backend.modules.auth.dto.request.ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
