package com.foodorderingapp.backend.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private UUID buildingId;
    private String avatarUrl;
}