package com.foodorderingapp.backend.modules.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private Long totalUsers;
    private Long totalShops;
    private Long pendingShops;
    private Long approvedShops;
    private Long rejectedShops;
    private Long bannedShops;
    private BigDecimal totalSystemRevenue;
    private Long totalSystemOrders;
    private List<DailyOrderDto> dailyOrders;
}
