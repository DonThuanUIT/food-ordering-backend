package com.foodorderingapp.backend.modules.order.dto.response;

import java.time.LocalDate;

public record DailyOrderDto(
        LocalDate date,
        Long orderCount
) {}
