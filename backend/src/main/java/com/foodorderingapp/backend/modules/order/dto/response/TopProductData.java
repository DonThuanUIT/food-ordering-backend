package com.foodorderingapp.backend.modules.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductData {
    private UUID foodId;
    private String foodName;
    private Long quantitySold;
    private Long revenue;
}
