package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.core.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserId (UUID userId);
    List<Order> findByUserPhone(String phone);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shop s " +
            "WHERE o.user.phone = :phone " +
            "AND o.status IN :statuses")
    List<Order> findActiveOrdersByPhone(@Param("phone") String phone,
                                        @Param("statuses") List<OrderStatus> statuses);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails " +
            "JOIN o.user u " +
            "WHERE u.phone = :phone " +
            "AND o.status IN ('COMPLETED', 'CANCELLED') " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrderHistoryByPhone(@Param("phone") String phone);

    @Query("SELECT o FROM Order o JOIN o.user u WHERE u.phone = :phone AND o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    List<Order> findCompletedOrdersBetween(@Param("phone") String phone, @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderDetails " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.shop " +
            "WHERE o.shop.id = :shopId " +
            "AND (:status IS NULL OR o.status = :status) " +
            "ORDER BY o.createdAt DESC")
    List<Order> findByShopIdAndStatus(@Param("shopId") UUID shopId,
                                      @Param("status") OrderStatus status);
}
