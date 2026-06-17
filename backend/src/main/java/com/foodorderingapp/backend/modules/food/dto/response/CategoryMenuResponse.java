package com.foodorderingapp.backend.modules.food.dto.response;

import java.util.List;
import java.util.UUID;

public record CategoryMenuResponse(UUID id,
                                   String name,
                                   List<FoodResponse> foods) { }
