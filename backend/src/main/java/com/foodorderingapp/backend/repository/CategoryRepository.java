package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findAllByShopId(UUID shopId);

    boolean existsByNameAndShopId(String name, UUID shopId);

    Optional<Category> findByIdAndShopId(UUID id, UUID shopId);
}
