package com.foodorderingapp.backend.modules.order.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private UserShort user;

    @Data
    @Builder
    public static class UserShort {
        private String fullName;
    }
}
