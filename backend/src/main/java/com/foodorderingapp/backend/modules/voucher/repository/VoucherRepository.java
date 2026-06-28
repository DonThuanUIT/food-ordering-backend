package com.foodorderingapp.backend.modules.voucher.repository;

import com.foodorderingapp.backend.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    List<Voucher> findAllByShopId(UUID shopId);

    Optional<Voucher> findByShopIdAndCode(UUID shopId, String code);

    Optional<Voucher> findByCode(String code);

    @Query("SELECT v FROM Voucher v LEFT JOIN FETCH v.foods WHERE v.shop.id = :shopId AND v.isActive = true AND (v.startDate IS NULL OR v.startDate <= :now) AND (v.endDate IS NULL OR v.endDate >= :now)")
    List<Voucher> findActiveVouchers(UUID shopId, LocalDateTime now);

    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "DELETE FROM voucher_foods WHERE food_id = :foodId", nativeQuery = true)
    void deleteVoucherFoodAssociations(@org.springframework.data.repository.query.Param("foodId") UUID foodId);
}
