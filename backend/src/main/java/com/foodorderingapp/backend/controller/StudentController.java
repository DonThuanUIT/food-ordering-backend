package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.UpdateProfileRequest;
import com.foodorderingapp.backend.dto.response.SpendingSummaryResponse;
import com.foodorderingapp.backend.dto.response.StudentReviewResponse;
import com.foodorderingapp.backend.dto.response.UserProfileResponse;
import com.foodorderingapp.backend.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(studentService.getMyProfile(principal.getName()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @RequestBody UpdateProfileRequest request,
            Principal principal) {
        return ResponseEntity.ok(studentService.updateMyProfile(principal.getName(), request));
    }

    @GetMapping("/me/spending-summary")
    public ResponseEntity<SpendingSummaryResponse> getSpendingSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        return ResponseEntity.ok(studentService.getSpendingSummary(principal.getName(), from, to));
    }

    @GetMapping("/me/reviews")
    public ResponseEntity<List<StudentReviewResponse>> getMyReviews(Principal principal) {
        return ResponseEntity.ok(studentService.getMyReviews(principal.getName()));
    }
}