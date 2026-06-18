package com.foodorderingapp.backend.modules.voucher.controller;

import com.foodorderingapp.backend.modules.voucher.dto.request.VoucherCreateRequest;
import com.foodorderingapp.backend.modules.voucher.dto.response.VoucherResponse;
import com.foodorderingapp.backend.modules.voucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/vendor/shops/{shopId}/vouchers")
@RequiredArgsConstructor
public class VendorVoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(
            @PathVariable UUID shopId,
            @Valid @RequestBody VoucherCreateRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        VoucherResponse response = voucherService.createVoucher(shopId, request, vendorPhone);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getShopVouchers(
            @PathVariable UUID shopId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        List<VoucherResponse> responses = voucherService.getShopVouchers(shopId, vendorPhone);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> getVoucherById(
            @PathVariable UUID shopId,
            @PathVariable UUID voucherId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        VoucherResponse response = voucherService.getVoucherById(shopId, voucherId, vendorPhone);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable UUID shopId,
            @PathVariable UUID voucherId,
            @Valid @RequestBody VoucherCreateRequest request,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        VoucherResponse response = voucherService.updateVoucher(shopId, voucherId, request, vendorPhone);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(
            @PathVariable UUID shopId,
            @PathVariable UUID voucherId,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        voucherService.deleteVoucher(shopId, voucherId, vendorPhone);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{voucherId}/status")
    public ResponseEntity<VoucherResponse> toggleVoucherStatus(
            @PathVariable UUID shopId,
            @PathVariable UUID voucherId,
            @RequestBody Map<String, Boolean> body,
            Principal principal
    ) {
        String vendorPhone = principal.getName();
        Boolean isActive = body.get("isActive");
        if (isActive == null) {
            isActive = true;
        }
        VoucherResponse response = voucherService.toggleVoucherStatus(shopId, voucherId, isActive, vendorPhone);
        return ResponseEntity.ok(response);
    }
}
