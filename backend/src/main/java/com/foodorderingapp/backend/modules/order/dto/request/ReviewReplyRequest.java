package com.foodorderingapp.backend.modules.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewReplyRequest {
    @NotBlank(message = "Nội dung phản hồi không được trống")
    private String replyText;

    private String reviewType; // "DELIVERY", "SHOP", "FOOD"
}
