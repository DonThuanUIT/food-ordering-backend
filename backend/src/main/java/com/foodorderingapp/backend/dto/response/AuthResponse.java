package com.foodorderingapp.backend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AuthResponse {
    private String message;
    private String phone;
}
