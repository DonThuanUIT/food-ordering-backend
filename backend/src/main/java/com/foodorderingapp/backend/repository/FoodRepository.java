package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {
    @Query("SELECT f FROM Food f JOIN FETCH f.category WHERE f.shop.id = :shopId")
    List<Food> findAllByShopIdWithCategory(@Param("shopId") UUID shopId);
}
