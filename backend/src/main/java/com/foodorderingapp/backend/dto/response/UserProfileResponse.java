package com.foodorderingapp.backend.dto.response;

import com.foodorderingapp.backend.entity.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String phone;
    private String fullName;
    private String email;
    private UserRole role;
    private UUID buildingId;
    private String buildingName;
    private String avatarUrl;
}