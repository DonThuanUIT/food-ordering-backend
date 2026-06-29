package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.modules.order.dto.request.ReviewReplyRequest;
import com.foodorderingapp.backend.modules.order.dto.response.ReviewReplyResponse;

import java.util.List;
import java.util.UUID;

public interface ReviewReplyService {
    List<ReviewReplyResponse> getReplies(UUID reviewId);
    ReviewReplyResponse createReply(UUID reviewId, ReviewReplyRequest request, String phone);
    ReviewReplyResponse updateReply(UUID replyId, ReviewReplyRequest request, String phone);
    void deleteReply(UUID replyId, String phone);
}
