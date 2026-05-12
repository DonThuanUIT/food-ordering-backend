package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodRequest {

    @NotNull(message = "Danh mục không được để trống")
    private UUID categoryId;

    @NotBlank(message = "Tên món ăn không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá tiền không được để trống")
    @DecimalMin(value = "0.01", message = "Giá tiền phải lớn hơn 0")
    private BigDecimal price;

    private String imageUrl;
}
