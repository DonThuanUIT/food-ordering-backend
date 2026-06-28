package com.foodorderingapp.backend.modules.cart.repository;

import com.foodorderingapp.backend.entity.CartItem;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID>{
    @Query("SELECT ci FROM CartItem ci " +
            "JOIN FETCH ci.food f " +
            "JOIN FETCH f.shop s " +
            "WHERE ci.cart.user.phone = :phone")
    List<CartItem> findAllByUserPhone(@Param("phone") String phone);
    Optional<CartItem> findByCartIdAndFoodId(UUID cartId, UUID foodId);
    @Modifying
    @Query("DELETE FROM CartItem ci " +
            "WHERE ci.food.shop.id = :shopId " +
            "AND ci.cart.user.phone = :phone")
    void deleteAllByShopIdAndUserPhone(@Param("shopId") UUID shopId, @Param("phone") String phone);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.food.id = :foodId")
    void deleteAllByFoodId(@Param("foodId") UUID foodId);
}
