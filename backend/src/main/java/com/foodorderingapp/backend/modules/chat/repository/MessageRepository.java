package com.foodorderingapp.backend.modules.chat.repository;

import com.foodorderingapp.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId);

    Optional<Message> findFirstByRoomIdOrderByCreatedAtDesc(UUID roomId);

    // Đếm tin nhắn chưa đọc khi ĐÃ CÓ mốc thời gian đọc
    long countByRoomIdAndSenderIdNotAndCreatedAtAfter(UUID roomId, UUID senderId, LocalDateTime lastReadAt);

    // Đếm tin nhắn chưa đọc khi CHƯA TỪNG mở phòng chat (lastReadAt = null)
    long countByRoomIdAndSenderIdNot(UUID roomId, UUID senderId);
}