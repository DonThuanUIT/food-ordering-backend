package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {
    List<Shop> findAllByOwnerId(UUID ownerId);
    Page<Shop> findAllByStatusAndIsActiveTrue (ShopStatus status, Pageable pageable);
    boolean existsByNameAndOwnerId(String name, UUID ownerId);
    @Query("SELECT DISTINCT s FROM Shop s " +
            "LEFT JOIN s.foods f " +
            "WHERE (CAST(function('unaccent', LOWER(s.name)) as String) LIKE CAST(function('unaccent', LOWER(CONCAT('%', :keyword, '%'))) as String) " +
            "OR CAST(function('unaccent', LOWER(f.name)) as String) LIKE CAST(function('unaccent', LOWER(CONCAT('%', :keyword, '%'))) as String)) " +
            "AND s.status = :status " +
            "AND s.isActive = true")
    Page<Shop> searchShops(@Param("keyword") String keyword,@Param("status") ShopStatus status, Pageable pageable);
}
