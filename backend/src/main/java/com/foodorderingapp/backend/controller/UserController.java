package com.foodorderingapp.backend.controller;

import com.foodorderingapp.backend.dto.request.ProfileRequest;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping("me")
    public ResponseEntity<User> getMyProfile(){
        return ResponseEntity.ok(userService.getMyProfile());
    }
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody ProfileRequest request) {
        User updateData = new User();
        updateData.setFullName(request.getFullName());
        updateData.setPhone(request.getPhone());

        User updatedUser = userService.updateProfile(updateData);
        return ResponseEntity.ok(updatedUser);
    }
}
