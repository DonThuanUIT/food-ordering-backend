package com.foodorderingapp.backend.modules.chat.repository;

import com.foodorderingapp.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("SELECT m FROM Message m JOIN FETCH m.sender WHERE m.room.id = :roomId ORDER BY m.createdAt ASC")
    List<Message> findByRoomIdOrderByCreatedAtAsc(@Param("roomId") UUID roomId);

    Optional<Message> findFirstByRoom_IdOrderByCreatedAtDesc(UUID roomId);

    long countByRoom_IdAndSender_IdNotAndCreatedAtAfter(UUID roomId, UUID senderId, LocalDateTime lastReadAt);

    long countByRoom_IdAndSender_IdNot(UUID roomId, UUID senderId);

    default Optional<Message> findFirstByRoomIdOrderByCreatedAtDesc(UUID roomId) {
        return findFirstByRoom_IdOrderByCreatedAtDesc(roomId);
    }

    default long countByRoomIdAndSenderIdNotAndCreatedAtAfter(UUID roomId, UUID senderId, LocalDateTime lastReadAt) {
        return countByRoom_IdAndSender_IdNotAndCreatedAtAfter(roomId, senderId, lastReadAt);
    }

    default long countByRoomIdAndSenderIdNot(UUID roomId, UUID senderId) {
        return countByRoom_IdAndSender_IdNot(roomId, senderId);
    }
}
