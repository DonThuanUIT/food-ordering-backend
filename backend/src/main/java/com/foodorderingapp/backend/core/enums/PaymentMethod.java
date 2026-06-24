package com.foodorderingapp.backend.core.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Tiền mặt"),
    BANK_TRANSFER("Chuyển khoản ngân hàng");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }
}