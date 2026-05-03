package com.foodorderingapp.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String roomNumber;
}
