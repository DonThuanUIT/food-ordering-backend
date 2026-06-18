package com.foodorderingapp.backend.modules.voucher.service;

import com.foodorderingapp.backend.modules.voucher.dto.request.VoucherCreateRequest;
import com.foodorderingapp.backend.modules.voucher.dto.response.VoucherResponse;

import java.util.List;
import java.util.UUID;

public interface VoucherService {
    VoucherResponse createVoucher(UUID shopId, VoucherCreateRequest request, String vendorPhone);
    List<VoucherResponse> getShopVouchers(UUID shopId, String vendorPhone);
    VoucherResponse getVoucherById(UUID shopId, UUID voucherId, String vendorPhone);
    VoucherResponse updateVoucher(UUID shopId, UUID voucherId, VoucherCreateRequest request, String vendorPhone);
    void deleteVoucher(UUID shopId, UUID voucherId, String vendorPhone);
    VoucherResponse toggleVoucherStatus(UUID shopId, UUID voucherId, Boolean isActive, String vendorPhone);
    List<VoucherResponse> getStudentActiveVouchers(UUID shopId);
}
