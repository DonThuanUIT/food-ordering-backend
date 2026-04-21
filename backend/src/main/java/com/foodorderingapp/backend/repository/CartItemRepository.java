package com.foodorderingapp.backend.repository;

import com.foodorderingapp.backend.entity.CartItem;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID>{

//     List<CartItem> findByUser_Id(UUID userId);
//
//     Optional<CartItem> findByUserIdAndFoodId(UUID cartId,  UUID foodId);
//
//    @Modifying
//    @Query("DELETE FROM CartItem ci WHERE ci.user.id = :userId")
//    void deleteAllByUserId(UUID userId);
}
