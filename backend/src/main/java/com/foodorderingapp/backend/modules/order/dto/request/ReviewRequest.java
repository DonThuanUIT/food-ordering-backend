package com.foodorderingapp.backend.modules.order.dto.request;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer rating;
    private String comment;
}
