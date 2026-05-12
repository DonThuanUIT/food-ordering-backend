package com.foodorderingapp.backend.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record FoodResponse(UUID id,
                          String name,
                          BigDecimal price,
                          Boolean isAvailable,
                          String imageUrl,
                          String description) {

}
