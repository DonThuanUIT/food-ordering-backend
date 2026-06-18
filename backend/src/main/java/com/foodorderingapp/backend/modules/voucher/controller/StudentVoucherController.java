package com.foodorderingapp.backend.modules.voucher.controller;

import com.foodorderingapp.backend.modules.voucher.dto.response.VoucherResponse;
import com.foodorderingapp.backend.modules.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shops/{shopId}/vouchers")
@RequiredArgsConstructor
public class StudentVoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getActiveVouchers(@PathVariable UUID shopId) {
        List<VoucherResponse> responses = voucherService.getStudentActiveVouchers(shopId);
        return ResponseEntity.ok(responses);
    }
}
