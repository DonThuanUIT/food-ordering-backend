package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.Order;
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
            "WHERE o.user.phone = :phone " +
            "AND o.status IN ('PENDING', 'PREPARING', 'DELIVERING')")
    List<Order> findActiveOrdersByPhone(@Param("phone") String phone);
}
