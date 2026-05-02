package com.foodorderingapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "shop_followers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_shop_follower", columnNames = {"user_id", "shop_id"})
        },
        indexes = {
                @Index(name = "idx_follower_user", columnList = "user_id"),
                @Index(name = "idx_follower_shop", columnList = "shop_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopFollower {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;
}
