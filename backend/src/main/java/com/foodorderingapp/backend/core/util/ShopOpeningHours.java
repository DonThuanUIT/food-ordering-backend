package com.foodorderingapp.backend.core.util;

import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.ShopSettings;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class ShopOpeningHours {
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private ShopOpeningHours() {
    }

    public static LocalTime[] effectiveOpeningHoursToday(Shop shop) {
        return effectiveOpeningHours(shop, LocalDate.now(BUSINESS_ZONE));
    }

    public static LocalTime[] effectiveOpeningHoursNow(Shop shop) {
        return effectiveOpeningHoursAt(shop, LocalDateTime.now(BUSINESS_ZONE));
    }

    public static LocalTime[] effectiveOpeningHoursAt(Shop shop, LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        LocalTime[] todayHours = effectiveOpeningHours(shop, dateTime.toLocalDate());
        if (isWithinSameDayOpeningWindow(time, todayHours[0], todayHours[1])) {
            return todayHours;
        }

        LocalTime[] yesterdayHours = effectiveOpeningHours(shop, dateTime.toLocalDate().minusDays(1));
        if (isOvernightHours(yesterdayHours[0], yesterdayHours[1])
                && !time.isAfter(yesterdayHours[1])) {
            return yesterdayHours;
        }

        return todayHours;
    }

    public static LocalTime[] effectiveOpeningHours(Shop shop, LocalDate date) {
        LocalTime open = shop.getOpenTime();
        LocalTime close = shop.getCloseTime();
        ShopSettings settings = shop.getSettings();

        if (settings != null) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY && settings.getSatOpenTime() != null && settings.getSatCloseTime() != null) {
                open = settings.getSatOpenTime();
                close = settings.getSatCloseTime();
            } else if (day == DayOfWeek.SUNDAY && settings.getSunOpenTime() != null && settings.getSunCloseTime() != null) {
                open = settings.getSunOpenTime();
                close = settings.getSunCloseTime();
            } else if (settings.getMonFriOpenTime() != null && settings.getMonFriCloseTime() != null) {
                open = settings.getMonFriOpenTime();
                close = settings.getMonFriCloseTime();
            }
        }

        return new LocalTime[]{open, close};
    }

    public static boolean isOpenNow(Shop shop) {
        return isOpenAt(shop, LocalDateTime.now(BUSINESS_ZONE));
    }

    public static boolean isOpenAt(Shop shop, LocalDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        LocalTime[] todayHours = effectiveOpeningHours(shop, dateTime.toLocalDate());
        if (isWithinSameDayOpeningWindow(time, todayHours[0], todayHours[1])) {
            return true;
        }

        LocalTime[] yesterdayHours = effectiveOpeningHours(shop, dateTime.toLocalDate().minusDays(1));
        return isOvernightHours(yesterdayHours[0], yesterdayHours[1])
                && !time.isAfter(yesterdayHours[1]);
    }

    private static boolean isWithinSameDayOpeningWindow(LocalTime time, LocalTime open, LocalTime close) {
        if (open == null || close == null) {
            return false;
        }
        if (open.equals(close)) {
            return true;
        }
        if (close.isAfter(open)) {
            return !time.isBefore(open) && !time.isAfter(close);
        }
        return !time.isBefore(open);
    }

    private static boolean isOvernightHours(LocalTime open, LocalTime close) {
        return open != null && close != null && close.isBefore(open);
    }
}
