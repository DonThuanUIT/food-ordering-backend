package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, UUID> {
}
