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
    @Query("SELECT fr FROM FoodReview fr JOIN FETCH fr.user u WHERE fr.food.id = :foodId ORDER BY fr.createdAt DESC")
    List<FoodReview> findByFoodIdOrderByCreatedAtDesc(@Param("foodId") UUID foodId);
    List<FoodReview> findByOrderId(UUID orderId);

    @Query("SELECT AVG(fr.rating) FROM FoodReview fr WHERE fr.food.id = :foodId")
    Double getAverageRatingForFood(@Param("foodId") UUID foodId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM FoodReview fr WHERE fr.food.id = :foodId")
    void deleteAllByFoodId(@Param("foodId") UUID foodId);
}
