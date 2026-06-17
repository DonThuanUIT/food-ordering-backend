package com.foodorderingapp.backend.core.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    DELIVERING,
    RECEIVED,
    FAILED,
    REJECTED,
    COMPLETED,
    CANCELLED
}