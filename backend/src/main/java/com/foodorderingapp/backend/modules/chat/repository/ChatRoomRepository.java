package com.foodorderingapp.backend.modules.chat.repository;

import com.foodorderingapp.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

    // Tìm phòng chat giữa 1 sinh viên cụ thể và 1 quán cụ thể
    Optional<ChatRoom> findByStudentIdAndShopId(UUID studentId, UUID shopId);

    @Query("SELECT r FROM ChatRoom r JOIN FETCH r.student JOIN FETCH r.shop s LEFT JOIN FETCH s.owner WHERE r.id = :roomId")
    Optional<ChatRoom> findByIdWithParticipants(@Param("roomId") UUID roomId);

    // Lấy danh sách tất cả phòng chat của một Sinh viên (để hiện màn hình danh sách chat)
    @Query("SELECT r FROM ChatRoom r JOIN FETCH r.shop WHERE r.student.id = :studentId ORDER BY r.updatedAt DESC")
    Iterable<ChatRoom> findAllByStudentId(UUID studentId);

    // Lấy danh sách tất cả phòng chat của một Chủ quán
    @Query("SELECT r FROM ChatRoom r JOIN FETCH r.student WHERE r.shop.owner.id = :vendorId ORDER BY r.updatedAt DESC")
    Iterable<ChatRoom> findAllByVendorId(UUID vendorId);
}
