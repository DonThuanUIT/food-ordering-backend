package com.foodorderingapp.backend.modules.chat.repository;

import com.foodorderingapp.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Lấy lịch sử tin nhắn của 1 phòng chat, sắp xếp cũ nhất ở trên
    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.room.id = :roomId ORDER BY m.createdAt ASC")
    List<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.room.id = :roomId AND m.sender.id != :userId AND m.isRead = false")
    void markOtherMessagesAsRead(UUID roomId, UUID userId);
}