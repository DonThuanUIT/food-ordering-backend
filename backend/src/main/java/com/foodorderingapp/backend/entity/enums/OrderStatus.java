package com.foodorderingapp.backend.entity.type;

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