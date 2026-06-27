package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.core.enums.OrderStatus;
import com.foodorderingapp.backend.modules.order.dto.response.DailyOrderDto;
import com.foodorderingapp.backend.modules.order.dto.response.VendorDashboardResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@Repository

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.user " +
            "JOIN FETCH o.shop " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") UUID orderId);

    List<Order> findByUserId (UUID userId);
    List<Order> findByUserPhone(String phone);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH o.shop s " +
            "LEFT JOIN FETCH o.shipper " +
            "WHERE u.phone = :phone " +
            "AND o.status IN :statuses " +
            "ORDER BY o.createdAt DESC")
    List<Order> findActiveOrdersByPhone(@Param("phone") String phone,
                                        @Param("statuses") List<OrderStatus> statuses);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "LEFT JOIN FETCH o.user u " +
            "LEFT JOIN FETCH o.shop s " +
            "LEFT JOIN FETCH o.shipper " +
            "WHERE u.phone = :phone " +
            "AND o.status IN ('COMPLETED', 'CANCELLED', 'RECEIVED', 'FAILED', 'REJECTED') " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrderHistoryByPhone(@Param("phone") String phone);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shop s " +
            "LEFT JOIN FETCH o.shipper " +
            "WHERE o.status = 'CONFIRMED' " +
            "AND o.shipper IS NULL " +
            "ORDER BY o.createdAt DESC")
    List<Order> findAvailableOrdersForDelivery();

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shop s " +
            "JOIN FETCH o.shipper sh " +
            "WHERE sh.phone = :phone " +
            "AND o.status IN ('CONFIRMED', 'DELIVERING')")
    List<Order> findActiveOrdersByShipper(@Param("phone") String phone);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shop s " +
            "JOIN FETCH o.shipper sh " +
            "WHERE sh.phone = :phone " +
            "AND o.status IN ('COMPLETED', 'RECEIVED') " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrderHistoryByShipper(@Param("phone") String phone);

    @Query("SELECT o FROM Order o JOIN o.user u WHERE u.phone = :phone AND o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    List<Order> findCompletedOrdersBetween(@Param("phone") String phone, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.food " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shop " +
            "LEFT JOIN FETCH o.shipper " +
            "WHERE o.shop.id = :shopId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByShopIdAndStatus(@Param("shopId") UUID shopId,
                                      @Param("status") OrderStatus status);
    @Query(value = "SELECT " +
            "  COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN total_price ELSE 0 END), 0) AS totalRevenue, " +
            "  COUNT(*) AS totalOrders, " +
            "  ROUND((COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END)::numeric / NULLIF(COUNT(*), 0)) * 100, 2) AS completionRate, " +
            "  ROUND(COALESCE(AVG(CASE WHEN status = 'COMPLETED' THEN total_price END), 0), 2) AS averageOrderValue " +
            "FROM orders " +
            "WHERE shop_id = :shopId " +
            "  AND created_at BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    VendorDashboardResponse getVendorDashboardStats(
            @Param("shopId") UUID shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    @Query(value = "SELECT TO_CHAR(created_at, 'YYYY-MM-DD') as date, " +
            "  COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN total_price ELSE 0 END), 0) as revenue, " +
            "  COUNT(*) as order_count " +
            "FROM orders " +
            "WHERE shop_id = :shopId AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY TO_CHAR(created_at, 'YYYY-MM-DD') " +
            "ORDER BY date ASC", nativeQuery = true)
    List<Map<String, Object>> getOrderTrends(
            @Param("shopId") UUID shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT " +
            "  od.food_name_snapshot as food_name, " + // Lấy thẳng tên từ snapshot
            "  SUM(od.quantity) as quantity_sold, " +
            "  SUM(od.price_snapshot * od.quantity) as revenue " + // Tính doanh thu từ price_snapshot
            "FROM order_details od " +
            "JOIN orders o ON od.order_id = o.id " +
            "WHERE o.shop_id = :shopId " +
            "  AND o.status = 'COMPLETED' " +
            "  AND o.created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY od.food_name_snapshot " + // Nhóm theo tên món ăn snapshot
            "ORDER BY quantity_sold DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> getTopSellingFoods(
            @Param("shopId") UUID shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT status, COUNT(*) as count " +
            "FROM orders " +
            "WHERE shop_id = :shopId AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY status", nativeQuery = true)
    List<Map<String, Object>> getOrderStatusBreakdown(
            @Param("shopId") UUID shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    @Query(value = "SELECT " +
            "  COALESCE(SUM(CASE WHEN status = 'COMPLETED' THEN total_price ELSE 0 END), 0) AS totalRevenue, " +
            "  COUNT(*) AS totalOrders " +
            "FROM orders", nativeQuery = true)
    Map<String, Object> getSystemOverviewStats();

    @Query(value = "SELECT CAST(created_at AS DATE) as date, COUNT(*) as orderCount " +
            "FROM orders " +
            "WHERE created_at >= :startDate " +
            "GROUP BY CAST(created_at AS DATE) " +
            "ORDER BY date ASC", nativeQuery = true)
    List<Map<String, Object>> getDailyOrderStats(
            @Param("startDate") LocalDateTime startDate
    );
}
