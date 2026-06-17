package com.foodorderingapp.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DropOffPointResponse {
    private UUID id;
    private String name;
    private UUID buildingId;
}