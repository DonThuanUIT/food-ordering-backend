package com.foodorderingapp.backend.modules.order.controller;

import com.foodorderingapp.backend.modules.order.AdminDashboardService;
import com.foodorderingapp.backend.modules.order.dto.response.AdminDashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<AdminDashboardDto> getAdminOverview() {
        AdminDashboardDto overview = adminDashboardService.getAdminOverview();
        return ResponseEntity.ok(overview);
    }
}
