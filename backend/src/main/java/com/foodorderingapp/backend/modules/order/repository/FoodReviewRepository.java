package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.FoodReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodReviewRepository extends JpaRepository<FoodReview, UUID> {
    List<FoodReview> findByFoodIdOrderByCreatedAtDesc(UUID foodId);
    List<FoodReview> findByOrderId(UUID orderId);

    @Query("SELECT AVG(fr.rating) FROM FoodReview fr WHERE fr.food.id = :foodId")
    Double getAverageRatingForFood(@Param("foodId") UUID foodId);
}
