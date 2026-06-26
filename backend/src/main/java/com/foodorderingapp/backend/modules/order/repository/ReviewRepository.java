package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByOrderId(UUID orderId);
    Optional<Review> findByOrderId(UUID orderId);

    @Query("SELECT r FROM Review r JOIN FETCH r.order o JOIN FETCH o.shop JOIN FETCH r.user u WHERE u.phone = :phone ORDER BY r.createdAt DESC")
    List<Review> findByUserPhoneWithOrderAndShop(@Param("phone") String phone);

    @Query("SELECT r FROM Review r JOIN FETCH r.user u JOIN r.order o WHERE o.shop.id = :shopId ORDER BY r.createdAt DESC")
    List<Review> findByShopIdOrderByCreatedAtDesc(@Param("shopId") UUID shopId);
}
