package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.core.enums.ShopStatus;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.order.dto.response.AdminDashboardDto;
import com.foodorderingapp.backend.modules.order.dto.response.DailyOrderDto;
import com.foodorderingapp.backend.modules.order.repository.OrderRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    @Override
    public AdminDashboardDto getAdminOverview() {
        long totalUsers = userRepository.count();
        long totalShops = shopRepository.count();
        long pendingShops = shopRepository.countByStatus(ShopStatus.PENDING);
        long approvedShops = shopRepository.countByStatus(ShopStatus.APPROVED);
        long rejectedShops = shopRepository.countByStatus(ShopStatus.REJECTED);
        long bannedShops = shopRepository.countByStatus(ShopStatus.BANNED);

        // 4. Lấy doanh thu toàn hệ thống từ OrderRepository
        Map<String, Object> orderStats = orderRepository.getSystemOverviewStats();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0L;

        if (orderStats != null) {
            if (orderStats.get("totalrevenue") != null) {
                totalRevenue = new BigDecimal(orderStats.get("totalrevenue").toString());
            }
            if (orderStats.get("totalorders") != null) {
                totalOrders = ((Number) orderStats.get("totalorders")).longValue();
            }
        }
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Map<String, Object>> rawDailyStats = orderRepository.getDailyOrderStats(sevenDaysAgo);

        List<DailyOrderDto> dailyOrders = rawDailyStats.stream()
                .map(row -> {
                    // Đọc key bằng chữ thường để an toàn với Native Query Postgres/MySQL
                    Object dateObj = row.get("date");
                    Object countObj = row.getOrDefault("ordercount", row.get("orderCount"));

                    java.time.LocalDate localDate = null;
                    if (dateObj instanceof java.sql.Date) {
                        localDate = ((java.sql.Date) dateObj).toLocalDate();
                    } else if (dateObj instanceof java.time.LocalDate) {
                        localDate = (java.time.LocalDate) dateObj;
                    }

                    long orderCount = countObj != null ? ((Number) countObj).longValue() : 0L;

                    return new DailyOrderDto(localDate, orderCount);
                })
                .collect(Collectors.toList());

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .totalShops(totalShops)
                .pendingShops(pendingShops)
                .approvedShops(approvedShops)
                .rejectedShops(rejectedShops)
                .bannedShops(bannedShops)
                .totalSystemRevenue(totalRevenue)
                .totalSystemOrders(totalOrders)
                .dailyOrders(dailyOrders)
                .build();
    }

}
