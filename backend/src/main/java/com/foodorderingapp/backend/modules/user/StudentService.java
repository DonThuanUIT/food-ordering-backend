package com.foodorderingapp.backend.modules.user;

import com.foodorderingapp.backend.modules.user.dto.request.UpdateProfileRequest;
import com.foodorderingapp.backend.modules.user.dto.response.SpendingSummaryResponse;
import com.foodorderingapp.backend.modules.user.dto.response.StudentReviewResponse;
import com.foodorderingapp.backend.modules.user.dto.response.UserProfileResponse;
import com.foodorderingapp.backend.entity.Building;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Review;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.building.repository.BuildingRepository;
import com.foodorderingapp.backend.modules.order.repository.OrderRepository;
import com.foodorderingapp.backend.modules.order.repository.ReviewRepository;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    public UserProfileResponse getMyProfile(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateMyProfile(String phone, UpdateProfileRequest request) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new AppException("Building not found", HttpStatus.NOT_FOUND));
            user.setBuilding(building);
        }

        User savedUser = userRepository.save(user);
        return mapToProfileResponse(savedUser);
    }

    public SpendingSummaryResponse getSpendingSummary(String phone, LocalDate fromDate, LocalDate toDate) {
        LocalDateTime from = (fromDate != null) ? fromDate.atStartOfDay() : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime to = (toDate != null) ? toDate.atTime(LocalTime.MAX) : LocalDateTime.now();

        LocalDateTime effectiveFrom = from;
        LocalDateTime effectiveTo = to;
        List<Order> completedOrders = orderRepository.findCompletedOrdersBetween(phone, effectiveFrom, effectiveTo);

        BigDecimal totalSpent = completedOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> breakdownMap = new LinkedHashMap<>();
        LocalDate start = effectiveFrom.toLocalDate();
        LocalDate end = effectiveTo.toLocalDate();
        boolean groupByDay = ChronoUnit.DAYS.between(start, end) <= 31;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (groupByDay) {
                String dayKey = date + " - " + date;
                breakdownMap.putIfAbsent(dayKey, BigDecimal.ZERO);
            } else {
                LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = weekStart.plusDays(6);
                String weekKey = weekStart + " - " + weekEnd;
                breakdownMap.putIfAbsent(weekKey, BigDecimal.ZERO);
            }
        }

        for (Order order : completedOrders) {
            LocalDateTime spendingTime = order.getCompletedAt() != null ? order.getCompletedAt() : order.getCreatedAt();
            LocalDate orderDate = spendingTime.toLocalDate();
            String key;
            if (groupByDay) {
                key = orderDate + " - " + orderDate;
            } else {
                LocalDate weekStart = orderDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = weekStart.plusDays(6);
                key = weekStart + " - " + weekEnd;
            }
            breakdownMap.merge(key, order.getTotalPrice(), BigDecimal::add);
        }

        List<SpendingSummaryResponse.SpendingBreakdown> breakdown = breakdownMap.entrySet().stream()
                .map(entry -> SpendingSummaryResponse.SpendingBreakdown.builder()
                        .period(entry.getKey())
                        .total(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return SpendingSummaryResponse.builder()
                .totalSpent(totalSpent)
                .breakdown(breakdown)
                .build();
    }

    public List<StudentReviewResponse> getMyReviews(String phone) {
        List<Review> reviews = reviewRepository.findByUserPhoneWithOrderAndShop(phone);

        return reviews.stream()
                .map(r -> StudentReviewResponse.builder()
                        .id(r.getId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .createdAt(r.getCreatedAt())
                        .orderId(r.getOrder().getId())
                        .shopName(r.getOrder().getShop().getName())
                        .totalPrice(r.getOrder().getTotalPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .buildingId(user.getBuilding() != null ? user.getBuilding().getId() : null)
                .buildingName(user.getBuilding() != null ? user.getBuilding().getName() : null)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
