package com.foodorderingapp.backend.dto.response;

import com.foodorderingapp.backend.entity.enums.UserRole;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String phone,
        String fullName,
        String email,
        UserRole role,
        Boolean isLocked
) {}