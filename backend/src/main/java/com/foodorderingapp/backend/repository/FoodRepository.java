package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    boolean existsByNameAndShopId(String name, UUID shopId);

    Optional<Food> findByIdAndShopId(UUID id, UUID shopId);

    @Query("SELECT f FROM Food f JOIN FETCH f.category WHERE f.shop.id = :shopId " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId)")
    List<Food> findByShopIdAndOptionalCategory(@Param("shopId") UUID shopId,
                                               @Param("categoryId") UUID categoryId);

    @Query("SELECT f FROM Food f JOIN FETCH f.category WHERE f.shop.id = :shopId")
    List<Food> findAllByShopIdWithCategory(@Param("shopId") UUID shopId);
}