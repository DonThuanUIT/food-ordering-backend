package com.foodorderingapp.backend.modules.shop.repository;

import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.core.enums.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, UUID> {
    List<Shop> findAllByOwnerId(UUID ownerId);
    @Query("SELECT s FROM Shop s JOIN s.owner o " +
            "WHERE s.status = :status " +
            "AND s.isActive = true " +
            "AND (o.isLocked = false OR o.isLocked IS NULL)")
    Page<Shop> findVisibleStudentShops(@Param("status") ShopStatus status, Pageable pageable);
    boolean existsByNameAndOwnerId(String name, UUID ownerId);
    @Query("SELECT DISTINCT s FROM Shop s " +
            "JOIN s.owner o " +
            "LEFT JOIN s.foods f " +
            "WHERE (CAST(function('unaccent', LOWER(s.name)) as String) LIKE CAST(function('unaccent', LOWER(CONCAT('%', :keyword, '%'))) as String) " +
            "OR CAST(function('unaccent', LOWER(f.name)) as String) LIKE CAST(function('unaccent', LOWER(CONCAT('%', :keyword, '%'))) as String)) " +
            "AND s.status = :status " +
            "AND s.isActive = true " +
            "AND (o.isLocked = false OR o.isLocked IS NULL)")
    Page<Shop> searchShops(@Param("keyword") String keyword,@Param("status") ShopStatus status, Pageable pageable);
    Page<Shop> findAllByStatus(ShopStatus status, Pageable pageable);
    Optional<Shop> findByIdAndOwner_Phone(UUID id, String phone);
    @Query("SELECT COUNT(s) FROM Shop s WHERE s.status = :status")
    long countByStatus(@Param("status") ShopStatus status);
}
