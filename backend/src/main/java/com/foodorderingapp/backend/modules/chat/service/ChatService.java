package com.foodorderingapp.backend.modules.chat.service;

import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.chat.dto.request.SendMessageRequest;
import com.foodorderingapp.backend.entity.ChatRoom;
import com.foodorderingapp.backend.entity.Message;
import com.foodorderingapp.backend.modules.chat.repository.ChatRoomRepository;
import com.foodorderingapp.backend.modules.chat.repository.MessageRepository;
import com.foodorderingapp.backend.modules.notification.service.NotificationService;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;

    // Vũ khí tối thượng: WebSocket Template & Firebase Notification
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Transactional
    public void processMessage(SendMessageRequest request) {
        // 1. Xác định người đang gửi tin nhắn
        String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        // 2. Tìm hoặc tạo Phòng Chat
        ChatRoom room;
        if (request.getRoomId() != null) {
            room = chatRoomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng chat"));
        } else if (request.getShopId() != null) {
            Shop shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy quán ăn"));

            // Tìm xem phòng cũ có chưa, chưa có thì tạo mới
            room = chatRoomRepository.findByStudentIdAndShopId(sender.getId(), shop.getId())
                    .orElseGet(() -> {
                        ChatRoom newRoom = ChatRoom.builder()
                                .student(sender)
                                .shop(shop)
                                .build();
                        return chatRoomRepository.save(newRoom);
                    });
        } else {
            throw new IllegalArgumentException("Phải cung cấp roomId hoặc shopId");
        }

        // 3. Lưu tin nhắn vào Database
        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .content(request.getContent())
                .build();
        messageRepository.save(message);

        // Cập nhật lại thời gian phòng chat có tương tác mới nhất
        room.setUpdatedAt(LocalDateTime.now());
        chatRoomRepository.save(room);

        // 4. GỬI WEBSOCKET (Real-time)
        // Bắn vào kênh của phòng chat: /topic/chat/ROOM_UUID
        String destination = "/topic/chat/" + room.getId();
        messagingTemplate.convertAndSend(destination, message.getContent());
        log.info("📡 Đã bắn WebSocket tới {}", destination);

        // 5. GỬI FIREBASE NOTIFICATION (Offline)
        // Xác định người nhận là ai (Nếu tôi là Student -> Gửi cho Chủ quán. Ngược lại)
        User receiver = sender.getId().equals(room.getStudent().getId())
                ? room.getShop().getOwner()
                : room.getStudent();

        String notificationTitle = "Tin nhắn từ " + sender.getFullName();
        notificationService.notifyUser(receiver, notificationTitle, message.getContent(), "CHAT", room.getId());
    }

    public Iterable<ChatRoom> getUserChatRooms() {
        String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        // Dựa vào Role để gọi đúng hàm Repository (Tránh việc Sinh viên nhìn thấy phòng chat của Quán khác)
        String role = currentUser.getRole().name(); // Giả định getRole() trả về String. Nếu dự án dùng Enum thì là getRole().name()

        if ("VENDOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            return chatRoomRepository.findAllByVendorId(currentUser.getId());
        } else {
            return chatRoomRepository.findAllByStudentId(currentUser.getId());
        }
    }

    /**
     * API 2: Đánh dấu tin nhắn đã đọc
     */
    @Transactional
    public void markAsRead(UUID roomId) {
        String userPhone = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        // Kích hoạt câu lệnh UPDATE siêu tốc đã viết ở Repository
        messageRepository.markOtherMessagesAsRead(roomId, currentUser.getId());
    }
}