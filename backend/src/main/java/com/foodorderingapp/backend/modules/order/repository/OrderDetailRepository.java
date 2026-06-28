package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(od) > 0 FROM OrderDetail od WHERE od.food.id = :foodId AND od.order.status IN :activeStatuses")
    boolean existsByFoodIdAndOrderStatusIn(@org.springframework.data.repository.query.Param("foodId") UUID foodId, @org.springframework.data.repository.query.Param("activeStatuses") java.util.List<com.foodorderingapp.backend.core.enums.OrderStatus> activeStatuses);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE OrderDetail od SET od.food = null WHERE od.food.id = :foodId")
    void setFoodNullByFoodId(@org.springframework.data.repository.query.Param("foodId") UUID foodId);
}
