package com.foodorderingapp.backend.modules.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SpendingSummaryResponse {
    private BigDecimal totalSpent;
    private List<SpendingBreakdown> breakdown;

    @Data
    @Builder
    public static class SpendingBreakdown {
        private String period;
        private BigDecimal total;
    }
}