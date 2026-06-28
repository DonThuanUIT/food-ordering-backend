package com.foodorderingapp.backend.modules.shop.repository;

import com.foodorderingapp.backend.entity.ShopFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopFollowerRepository extends JpaRepository<ShopFollower, UUID> {
    boolean existsByUserIdAndShopId(UUID userId, UUID shopId);
    Optional<ShopFollower> findByUserIdAndShopId(UUID userId, UUID shopId);
    
    @Query("SELECT sf FROM ShopFollower sf JOIN FETCH sf.shop s WHERE sf.user.id = :userId")
    List<ShopFollower> findAllByUserId(@Param("userId") UUID userId);
}
