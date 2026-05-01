package com.foodorderingapp.backend.entity.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    DELIVERING,
    RECEIVED,
    FAILED,
    REJECTED
}