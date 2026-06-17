package com.foodorderingapp.backend.modules.user.dto.response;

import com.foodorderingapp.backend.core.enums.UserRole;
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