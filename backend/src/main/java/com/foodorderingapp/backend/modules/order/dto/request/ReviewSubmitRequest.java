package com.foodorderingapp.backend.modules.order.dto.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ReviewSubmitRequest {
    private Integer orderRating;     // Delivery/order rating (1-5)
    private String orderComment;     // Delivery/order comment
    
    private Integer shopRating;      // Shop rating (1-5)
    private String shopComment;      // Shop comment
    
    private List<FoodReviewItem> foodReviews; // Individual food item reviews

    @Data
    public static class FoodReviewItem {
        private UUID foodId;
        private Integer rating;
        private String comment;
    }
}
