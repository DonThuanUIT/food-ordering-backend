package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record ShopStatusRequest(
        @NotNull(message = "Trạng thái hoạt động không được để trống")
        Boolean isActive
) {
}
