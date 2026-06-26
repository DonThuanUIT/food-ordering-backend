package com.foodorderingapp.backend.modules.building.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BuildingResponse {
    private UUID id;
    private String name;
    private Double latitude;
    private Double longitude;
}