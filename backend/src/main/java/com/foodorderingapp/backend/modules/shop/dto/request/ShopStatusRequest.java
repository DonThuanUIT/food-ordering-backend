package com.foodorderingapp.backend.modules.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ShopStatusRequest(
        @NotBlank(message = "Trạng thái phê duyệt không được để trống")
        @Pattern(
                regexp = "APPROVED|REJECTED",
                message = "Trạng thái không hợp lệ! Admin chỉ được phép duyệt (APPROVED) hoặc từ chối (REJECTED)"
        )
        String status
) {
}
