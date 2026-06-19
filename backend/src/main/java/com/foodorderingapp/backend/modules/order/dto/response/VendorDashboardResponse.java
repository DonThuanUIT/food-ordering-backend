package com.foodorderingapp.backend.modules.order.dto.response;

import java.math.BigDecimal;

public interface VendorDashboardResponse {
    BigDecimal getTotalRevenue();
    Long getTotalOrders();
    Double getCompletionRate();
    BigDecimal getAverageOrderValue();

}
