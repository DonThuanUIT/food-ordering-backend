package com.foodorderingapp.backend.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String phone;
    private String password;
    private String fullName;
}
