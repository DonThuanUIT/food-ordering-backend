package com.foodorderingapp.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopCreateRequest {

    @NotBlank(message = "Shop name cannot be left blank")
    @Size(max = 255, message = "shop name cannot exceed 255 characters")
    private String name;

    private String description;

    @NotNull(message = "Opening hours cannot be left blank")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;

    @NotNull(message = "Closing hours cannot be left blank")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
}