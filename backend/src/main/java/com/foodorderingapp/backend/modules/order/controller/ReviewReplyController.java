package com.foodorderingapp.backend.modules.order.controller;

import com.foodorderingapp.backend.modules.order.ReviewReplyService;
import com.foodorderingapp.backend.modules.order.dto.request.ReviewReplyRequest;
import com.foodorderingapp.backend.modules.order.dto.response.ReviewReplyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewReplyController {

    private final ReviewReplyService replyService;

    @GetMapping("/{reviewId}/replies")
    public ResponseEntity<List<ReviewReplyResponse>> getReplies(@PathVariable UUID reviewId) {
        List<ReviewReplyResponse> replies = replyService.getReplies(reviewId);
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/{reviewId}/replies")
    public ResponseEntity<ReviewReplyResponse> createReply(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewReplyRequest request,
            Principal principal) {
        String phone = principal.getName();
        ReviewReplyResponse reply = replyService.createReply(reviewId, request, phone);
        return ResponseEntity.ok(reply);
    }

    @PutMapping("/replies/{replyId}")
    public ResponseEntity<ReviewReplyResponse> updateReply(
            @PathVariable UUID replyId,
            @Valid @RequestBody ReviewReplyRequest request,
            Principal principal) {
        String phone = principal.getName();
        ReviewReplyResponse reply = replyService.updateReply(replyId, request, phone);
        return ResponseEntity.ok(reply);
    }

    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable UUID replyId,
            Principal principal) {
        String phone = principal.getName();
        replyService.deleteReply(replyId, phone);
        return ResponseEntity.noContent().build();
    }
}
