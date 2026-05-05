package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {
    List<Shop> findAllByOwnerId(UUID ownerId);

    boolean existsByNameAndOwnerId(String name, UUID ownerId);
}
