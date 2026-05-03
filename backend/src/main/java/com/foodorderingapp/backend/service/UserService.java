package com.foodorderingapp.backend.service;

import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.exception.AppException;
import com.foodorderingapp.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    public User getMyProfile(){
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(currentUserEmail).orElseThrow(()-> new AppException("Khong tim thay thong tin nguoi dung: " + currentUserEmail, HttpStatus.NOT_FOUND));

    }
    @Transactional
    public User updateProfile(User updateData){
        User user = getMyProfile();
        user.setFullName(updateData.getFullName());
        user.setPhone(updateData.getPhone());
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new AppException("Lỗi khi cập nhật dữ liệu vào hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
