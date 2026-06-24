package com.foodorderingapp.backend.modules.chat.controller;

import com.foodorderingapp.backend.modules.chat.dto.request.SendMessageRequest;
import com.foodorderingapp.backend.modules.chat.dto.response.ChatMessageResponse;
import com.foodorderingapp.backend.modules.chat.dto.response.ChatRoomResponse;
import com.foodorderingapp.backend.modules.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. Tạo/Lấy room vĩnh viễn theo ShopId
    @GetMapping("/shops/{shopId}/room")
    public ResponseEntity<ChatRoomResponse> getRoomByShop(@PathVariable UUID shopId) {
        return ResponseEntity.ok(chatService.getOrCreateRoomByShop(shopId));
    }

    // 2. Gửi tin nhắn (Trả về Object)
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendHttpMessage(@Valid @RequestBody SendMessageRequest request) {
        ChatMessageResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    // 3. Lịch sử tin nhắn của Room
    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getHistory(roomId));
    }

    // 4. Lấy danh sách Inbox (Kèm unread, last message, partner info)
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms() {
        return ResponseEntity.ok(chatService.getUserChatRooms());
    }

    // 5. Đánh dấu đã đọc
    @PutMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable UUID roomId) {
        chatService.markAsRead(roomId);
        return ResponseEntity.ok().build();
    }

    // 6. Tổng số tin nhắn chưa đọc (Badge icon)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getTotalUnreadCount() {
        return ResponseEntity.ok(chatService.getTotalUnreadCount());
    }

    // 7. Tin nhắn chưa đọc của 1 phòng (Nếu cần)
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<Long> getUnreadCountByRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(chatService.getUnreadCountByRoom(roomId));
    }

    // WebSocket Endpoint
    @MessageMapping("/chat.sendMessage")
    public void receiveWebSocketMessage(@Valid SendMessageRequest request) {
        chatService.processMessage(request);
    }
}