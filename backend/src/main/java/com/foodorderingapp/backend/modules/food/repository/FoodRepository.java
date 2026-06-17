package com.foodorderingapp.backend.modules.food.repository;

import com.foodorderingapp.backend.modules.food.dto.response.FoodExploreResponse;
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
    @Query("SELECT new com.foodorderingapp.backend.modules.food.dto.response.FoodExploreResponse(" +
            "f.id, f.name, f.price, f.imageUrl, f.description, s.id, s.name, c.name) " +
            "FROM Food f " +
            "JOIN f.shop s " +
            "JOIN f.category c " +
            "WHERE f.isAvailable = true " +
            "AND s.isActive = true " +
            "AND s.status = 'APPROVED' " +
            "AND (" +
            "  (s.openTime < s.closeTime AND :now BETWEEN s.openTime AND s.closeTime) OR " +
            "  (s.openTime > s.closeTime AND (:now >= s.openTime OR :now <= s.closeTime))" +
            ")")
    org.springframework.data.domain.Page<FoodExploreResponse> exploreFoods(
            @Param("now") java.time.LocalTime now,
            org.springframework.data.domain.Pageable pageable
    );

}
