package com.foodorderingapp.backend.modules.order.dto.response;

import java.math.BigDecimal;
import java.util.List;

public interface VendorDashboardResponse {
    BigDecimal getTotalRevenue();
    Long getTotalOrders();
    Double getCompletionRate();
    BigDecimal getAverageOrderValue();


}
