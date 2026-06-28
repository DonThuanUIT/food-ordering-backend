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

    // >>> PHASE 1: KTX Food Map - Lấy danh sách shop đã APPROVED, active, có tọa độ
    @Query("SELECT s FROM Shop s JOIN s.owner o " +
            "WHERE s.status = :status " +
            "AND s.isActive = true " +
            "AND s.latitude IS NOT NULL " +
            "AND s.longitude IS NOT NULL " +
            "AND (o.isLocked = false OR o.isLocked IS NULL)")
    List<Shop> findActiveShopsWithLocation(@Param("status") ShopStatus status);

    // >>> PHASE 1: AI Spatial - Haversine formula tìm shop trong bán kính (km)
    @Query(value = """
            SELECT s.* FROM shops s
            JOIN users u ON s.owner_id = u.id
            WHERE s.status = 'APPROVED'
            AND s.is_active = true
            AND s.latitude IS NOT NULL
            AND s.longitude IS NOT NULL
            AND (u.is_locked = false OR u.is_locked IS NULL)
            AND (
                6371 * acos(
                    cos(radians(:userLat)) * cos(radians(s.latitude))
                    * cos(radians(s.longitude) - radians(:userLng))
                    + sin(radians(:userLat)) * sin(radians(s.latitude))
                )
            ) <= :radiusKm
            ORDER BY (
                6371 * acos(
                    cos(radians(:userLat)) * cos(radians(s.latitude))
                    * cos(radians(s.longitude) - radians(:userLng))
                    + sin(radians(:userLat)) * sin(radians(s.latitude))
                )
            ) ASC
            """, nativeQuery = true)
    List<Shop> findShopsWithinRadius(@Param("userLat") double userLat,
                                     @Param("userLng") double userLng,
                                     @Param("radiusKm") double radiusKm);
}
