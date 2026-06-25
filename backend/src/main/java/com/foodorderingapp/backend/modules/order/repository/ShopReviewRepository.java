package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.ShopReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopReviewRepository extends JpaRepository<ShopReview, UUID> {
    boolean existsByOrderId(UUID orderId);
    Optional<ShopReview> findByOrderId(UUID orderId);
    List<ShopReview> findByShopIdOrderByCreatedAtDesc(UUID shopId);

    @Query("SELECT AVG(sr.rating) FROM ShopReview sr WHERE sr.shop.id = :shopId")
    Double getAverageRatingForShop(@Param("shopId") UUID shopId);
}
