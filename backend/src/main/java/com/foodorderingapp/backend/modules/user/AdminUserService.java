package com.foodorderingapp.backend.modules.user;

import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.modules.user.dto.response.UserResponse;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.core.enums.UserRole;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String search, UserRole role, Pageable pageable) {

        Specification<User> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (search != null && !search.isBlank()) {
                String keyword = "%" + search.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("phone"), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), keyword)
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable)
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getPhone(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole(),
                        u.getIsLocked()
                ));
    }

    @Transactional
    public void toggleUserLock(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng")); // Bạn có thể thay bằng AppException của dự án

        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Lỗi bảo mật: Không được phép khóa tài khoản ADMIN!");
        }

        user.setIsLocked(!user.getIsLocked());
        userRepository.save(user);
    }
    @Transactional
    public void updateUserLockStatus(UUID userId, boolean isLocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Không tìm thấy người dùng", HttpStatus.NOT_FOUND));

        if (user.getRole() == UserRole.ADMIN) {
            throw new AppException("Lỗi bảo mật: Không được phép khóa tài khoản ADMIN!", HttpStatus.BAD_REQUEST);
        }

        // Gán trực tiếp trạng thái truyền từ FE gửi lên
        user.setIsLocked(isLocked);
        userRepository.save(user);
    }
}