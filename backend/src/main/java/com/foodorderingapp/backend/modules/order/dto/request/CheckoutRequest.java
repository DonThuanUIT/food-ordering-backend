package com.foodorderingapp.backend.modules.order.dto.request;

import com.foodorderingapp.backend.core.enums.PaymentMethod;
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

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethod paymentMethod;

    // Voucher của Quán (Có thể null nếu không dùng)
    private String voucherCode;

    // Địa chỉ giao hàng
    @NotNull(message = "Vui lòng chọn tòa nhà")
    private UUID buildingId;

    @NotNull(message = "Vui lòng chọn điểm giao")
    private UUID dropOffPointId;

    private String note;
}