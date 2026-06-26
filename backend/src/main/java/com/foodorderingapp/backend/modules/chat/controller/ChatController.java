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

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 1. Tạo/Lấy room vĩnh viễn theo ShopId
    @GetMapping("/shops/{shopId}/room")
    public ResponseEntity<ChatRoomResponse> getRoomByShop(@PathVariable UUID shopId, Principal principal) {
        return ResponseEntity.ok(chatService.getOrCreateRoomByShop(shopId, principal.getName()));
    }

    // 2. Gửi tin nhắn (Trả về Object)
    @GetMapping("/orders/{orderId}/room")
    public ResponseEntity<ChatRoomResponse> getRoomByOrder(@PathVariable UUID orderId, Principal principal) {
        return ResponseEntity.ok(chatService.getOrCreateRoomByOrder(orderId, principal.getName()));
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendHttpMessage(@Valid @RequestBody SendMessageRequest request,
                                                                Principal principal) {
        ChatMessageResponse response = chatService.processMessage(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // 3. Lịch sử tin nhắn của Room
    @GetMapping("/{roomId}/history")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable UUID roomId, Principal principal) {
        return ResponseEntity.ok(chatService.getHistory(roomId, principal.getName()));
    }

    // 4. Lấy danh sách Inbox (Kèm unread, last message, partner info)
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(Principal principal) {
        return ResponseEntity.ok(chatService.getUserChatRooms(principal.getName()));
    }

    // 5. Đánh dấu đã đọc
    @PutMapping("/{roomId}/read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable UUID roomId, Principal principal) {
        chatService.markAsRead(roomId, principal.getName());
        return ResponseEntity.ok().build();
    }

    // 6. Tổng số tin nhắn chưa đọc (Badge icon)
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getTotalUnreadCount(Principal principal) {
        return ResponseEntity.ok(chatService.getTotalUnreadCount(principal.getName()));
    }

    // 7. Tin nhắn chưa đọc của 1 phòng (Nếu cần)
    @GetMapping("/rooms/{roomId}/unread-count")
    public ResponseEntity<Long> getUnreadCountByRoom(@PathVariable UUID roomId, Principal principal) {
        return ResponseEntity.ok(chatService.getUnreadCountByRoom(roomId, principal.getName()));
    }

    // WebSocket Endpoint
    @MessageMapping("/chat.sendMessage")
    public void receiveWebSocketMessage(@Valid SendMessageRequest request, Principal principal) {
        chatService.processMessage(request, principal.getName());
    }
}
