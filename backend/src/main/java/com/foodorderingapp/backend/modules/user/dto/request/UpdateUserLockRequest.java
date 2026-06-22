package com.foodorderingapp.backend.modules.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserLockRequest {
    @NotNull(message = "Trạng thái locked không được để trống")
    private Boolean locked;
}
