package com.foodorderingapp.backend.core.util;

import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.ShopSettings;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopOpeningHoursTest {

    @Test
    void earlyMorningUsesPreviousDayOvernightHours() {
        Shop shop = shopWithWeekendHours(
                LocalTime.of(22, 0), LocalTime.of(2, 0),
                LocalTime.of(10, 0), LocalTime.of(20, 0)
        );
        LocalDateTime sundayAtOne = LocalDateTime.of(2026, 6, 28, 1, 0);

        assertTrue(ShopOpeningHours.isOpenAt(shop, sundayAtOne));
        assertArrayEquals(
                new LocalTime[]{LocalTime.of(22, 0), LocalTime.of(2, 0)},
                ShopOpeningHours.effectiveOpeningHoursAt(shop, sundayAtOne)
        );
    }

    @Test
    void earlyMorningDoesNotUseFutureOvernightHoursFromSameDay() {
        Shop shop = shopWithWeekendHours(
                LocalTime.of(10, 0), LocalTime.of(20, 0),
                LocalTime.of(22, 0), LocalTime.of(2, 0)
        );
        LocalDateTime sundayAtOne = LocalDateTime.of(2026, 6, 28, 1, 0);

        assertFalse(ShopOpeningHours.isOpenAt(shop, sundayAtOne));
    }

    private Shop shopWithWeekendHours(LocalTime saturdayOpen,
                                      LocalTime saturdayClose,
                                      LocalTime sundayOpen,
                                      LocalTime sundayClose) {
        Shop shop = Shop.builder()
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(17, 0))
                .build();
        ShopSettings settings = ShopSettings.builder()
                .shop(shop)
                .monFriOpenTime(LocalTime.of(8, 0))
                .monFriCloseTime(LocalTime.of(17, 0))
                .satOpenTime(saturdayOpen)
                .satCloseTime(saturdayClose)
                .sunOpenTime(sundayOpen)
                .sunCloseTime(sundayClose)
                .build();
        shop.setSettings(settings);
        return shop;
    }
}
