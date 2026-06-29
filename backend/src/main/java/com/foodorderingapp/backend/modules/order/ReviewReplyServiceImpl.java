package com.foodorderingapp.backend.modules.order;

import com.foodorderingapp.backend.entity.ReviewReply;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewReplyRequest;
import com.foodorderingapp.backend.modules.order.dto.response.ReviewReplyResponse;
import com.foodorderingapp.backend.modules.order.repository.FoodReviewRepository;
import com.foodorderingapp.backend.modules.order.repository.ReviewReplyRepository;
import com.foodorderingapp.backend.modules.order.repository.ReviewRepository;
import com.foodorderingapp.backend.modules.order.repository.ShopReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private final ReviewReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ShopReviewRepository shopReviewRepository;
    private final FoodReviewRepository foodReviewRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReplyResponse> getReplies(UUID reviewId) {
        return replyRepository.findByReviewIdOrderByCreatedAtAsc(reviewId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReviewReplyResponse createReply(UUID reviewId, ReviewReplyRequest request, String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        // Determine review type
        String reviewType = request.getReviewType();
        if (reviewType == null || reviewType.trim().isEmpty()) {
            if (reviewRepository.existsById(reviewId)) {
                reviewType = "DELIVERY";
            } else if (shopReviewRepository.existsById(reviewId)) {
                reviewType = "SHOP";
            } else if (foodReviewRepository.existsById(reviewId)) {
                reviewType = "FOOD";
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá gốc");
            }
        }

        ReviewReply reply = ReviewReply.builder()
                .reviewId(reviewId)
                .reviewType(reviewType.trim().toUpperCase())
                .user(user)
                .replyText(request.getReplyText())
                .build();

        ReviewReply saved = replyRepository.save(reply);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ReviewReplyResponse updateReply(UUID replyId, ReviewReplyRequest request, String phone) {
        ReviewReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi"));

        if (!reply.getUser().getPhone().equals(phone)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa phản hồi này");
        }

        reply.setReplyText(request.getReplyText());
        ReviewReply updated = replyRepository.save(reply);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReply(UUID replyId, String phone) {
        ReviewReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi"));

        if (!reply.getUser().getPhone().equals(phone)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa phản hồi này");
        }

        replyRepository.delete(reply);
    }

    private ReviewReplyResponse mapToResponse(ReviewReply reply) {
        return ReviewReplyResponse.builder()
                .id(reply.getId())
                .reviewId(reply.getReviewId())
                .replyText(reply.getReplyText())
                .senderName(reply.getUser().getFullName())
                .senderRole(reply.getUser().getRole().name())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}
