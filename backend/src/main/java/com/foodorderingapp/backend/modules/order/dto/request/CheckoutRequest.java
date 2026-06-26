package com.foodorderingapp.backend.modules.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CheckoutRequest {

    @NotNull(message = "Phải xác định quán ăn cần thanh toán")
    private UUID shopId;

    @NotEmpty(message = "Vui lòng chọn ít nhất 1 món để thanh toán")
    private List<UUID> cartItemIds; // Chỉ lấy những món mà user đánh dấu tick ✅ trong giỏ hàng

    // Voucher của Quán (Có thể null nếu không dùng)
    private String voucherCode;

    // Địa chỉ giao hàng
    @NotNull(message = "Vui lòng chọn tòa nhà")
    private UUID buildingId;

    private UUID dropOffPointId;

    private String note;
}
