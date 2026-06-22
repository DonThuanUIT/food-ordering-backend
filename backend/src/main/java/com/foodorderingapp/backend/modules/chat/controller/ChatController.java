package com.foodorderingapp.backend.modules.chat.controller;

import com.foodorderingapp.backend.modules.chat.dto.request.SendMessageRequest;
import com.foodorderingapp.backend.entity.ChatRoom;
import com.foodorderingapp.backend.entity.Message;
import com.foodorderingapp.backend.modules.chat.repository.MessageRepository;
import com.foodorderingapp.backend.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final MessageRepository messageRepository;

    @PostMapping("/send")
    public ResponseEntity<Void> sendHttpMessage(@Valid @RequestBody SendMessageRequest request) {
        chatService.processMessage(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<Message>> getChatHistory(@PathVariable UUID roomId) {
        return ResponseEntity.ok(messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId));
    }

    @MessageMapping("/chat.sendMessage")
    public void receiveWebSocketMessage(@Valid SendMessageRequest request) {
        chatService.processMessage(request);
    }

    /**
     * Lấy danh sách các phòng chat (Hộp thư đến) đã hoàn thiện
     */
    @GetMapping("/rooms")
    public ResponseEntity<Iterable<ChatRoom>> getChatRooms() {
        return ResponseEntity.ok(chatService.getUserChatRooms());
    }

    /**
     * API Đánh dấu đã đọc đã hoàn thiện
     */
    @PutMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable UUID roomId) {
        chatService.markAsRead(roomId);
        return ResponseEntity.ok().build();
    }
}