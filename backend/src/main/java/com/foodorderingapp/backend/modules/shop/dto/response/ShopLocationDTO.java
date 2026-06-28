package com.foodorderingapp.backend.modules.shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopLocationDTO {
    private UUID id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String coverUrl;
    private Double rating;
    private boolean currentlyOpen;
}