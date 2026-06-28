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
public class FollowerResponse {
    private UUID id;
    private UUID userId;
    private String name;
    private String phone;
    private String email;
}
