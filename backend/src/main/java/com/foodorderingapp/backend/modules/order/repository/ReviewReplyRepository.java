package com.foodorderingapp.backend.modules.order.repository;

import com.foodorderingapp.backend.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, UUID> {
    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(UUID reviewId);
}
