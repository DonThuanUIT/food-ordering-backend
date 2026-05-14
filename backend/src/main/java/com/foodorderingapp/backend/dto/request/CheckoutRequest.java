package com.foodorderingapp.backend.dto.request;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String buildingName;
    private String dropOffPoint;
}
