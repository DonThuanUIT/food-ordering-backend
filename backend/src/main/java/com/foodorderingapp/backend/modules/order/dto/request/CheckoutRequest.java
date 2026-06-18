package com.foodorderingapp.backend.modules.order.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CheckoutRequest {
    private String buildingName;
    private String dropOffPoint;
    private UUID buildingId;
    private UUID dropOffPointId;
    private String voucherCode;
}
