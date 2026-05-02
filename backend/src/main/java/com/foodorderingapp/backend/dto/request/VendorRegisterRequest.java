package com.foodorderingapp.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class VendorRegisterRequest extends BaseRegisterRequest {
    @NotBlank(message = "Vendor's name cannot be blank !")
    private String shopName;

    private String description;

    private LocalTime openTime;

    private LocalTime closeTime;
}