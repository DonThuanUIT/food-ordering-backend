package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CartRequest(@NotNull(message = "ID món ăn không được để trống")
                          UUID foodId,

                          @NotNull(message = "Số lượng không được để trống")
                          @Min(value = 1, message = "Số lượng món ăn phải ít nhất là 1")
                          Integer quantity,

                          String note) {
}
