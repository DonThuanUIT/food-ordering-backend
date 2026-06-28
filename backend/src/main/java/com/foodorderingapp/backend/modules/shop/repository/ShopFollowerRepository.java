package com.foodorderingapp.backend.modules.shop.repository;

import com.foodorderingapp.backend.entity.ShopFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopFollowerRepository extends JpaRepository<ShopFollower, UUID> {
    boolean existsByUserPhoneAndShopId(String phone, UUID shopId);
    Optional<ShopFollower> findByUserPhoneAndShopId(String phone, UUID shopId);
}
