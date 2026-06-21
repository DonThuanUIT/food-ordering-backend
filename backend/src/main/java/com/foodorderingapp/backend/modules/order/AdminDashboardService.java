package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.response.AdminDashboardDto;

public interface AdminDashboardService {
    AdminDashboardDto getAdminOverview();
}
