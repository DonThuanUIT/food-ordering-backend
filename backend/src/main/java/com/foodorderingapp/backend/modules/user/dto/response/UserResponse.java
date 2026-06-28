package com.foodorderingapp.backend.modules.user.dto.response;

import com.foodorderingapp.backend.core.enums.UserRole;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String phone,
        String fullName,
        String email,
        String avatarUrl,
        UserRole role,
        Boolean isLocked
) {}
