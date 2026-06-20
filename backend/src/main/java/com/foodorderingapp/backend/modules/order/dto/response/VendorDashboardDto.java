package com.foodorderingapp.backend.modules.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDashboardDto {
    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Double completionRate;
    private BigDecimal averageOrderValue;
    private List<TrendData> orderTrends;
    private List<TopProductData> topSellingProducts;
    private Map<String, Long> orderStatusBreakdown;
}
