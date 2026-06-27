package com.foodorderingapp.backend.modules.chat.service;

import com.foodorderingapp.backend.core.exception.AppException;
import com.foodorderingapp.backend.entity.ChatRoom;
import com.foodorderingapp.backend.entity.Message;
import com.foodorderingapp.backend.entity.Order;
import com.foodorderingapp.backend.entity.Shop;
import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.modules.auth.repository.UserRepository;
import com.foodorderingapp.backend.modules.chat.dto.request.SendMessageRequest;
import com.foodorderingapp.backend.modules.chat.dto.response.ChatMessageResponse;
import com.foodorderingapp.backend.modules.chat.dto.response.ChatRoomResponse;
import com.foodorderingapp.backend.modules.chat.repository.ChatRoomRepository;
import com.foodorderingapp.backend.modules.chat.repository.MessageRepository;
import com.foodorderingapp.backend.modules.notification.service.NotificationService;
import com.foodorderingapp.backend.modules.order.repository.OrderRepository;
import com.foodorderingapp.backend.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    private User getCurrentUser(String userPhone) {
        return userRepository.findByPhone(userPhone)
                .orElseThrow(() -> new AppException("Không tìm thấy User", HttpStatus.UNAUTHORIZED));
    }

    // Bảo vệ quyền truy cập phòng chat
    private void validateUserInRoom(ChatRoom room, User user) {
        boolean isStudent = room.getStudent().getId().equals(user.getId());
        boolean isVendor = room.getShop().getOwner().getId().equals(user.getId());
        if (!isStudent && !isVendor) {
            throw new AppException("Bạn không có quyền truy cập phòng chat này", HttpStatus.FORBIDDEN);
        }
    }

    // Lấy hoặc tạo phòng chat vĩnh viễn theo Shop
    @Transactional
    public ChatRoomResponse getOrCreateRoomByShop(UUID shopId, String userPhone) {
        User currentUser = getCurrentUser(userPhone);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException("Không tìm thấy quán ăn", HttpStatus.NOT_FOUND));

        ChatRoom room = chatRoomRepository.findByStudentIdAndShopId(currentUser.getId(), shop.getId())
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .student(currentUser)
                            .shop(shop)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });

        return buildChatRoomResponse(room, currentUser);
    }

    @Transactional
    public ChatRoomResponse getOrCreateRoomByOrder(UUID orderId, String userPhone) {
        User currentUser = getCurrentUser(userPhone);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Không tìm thấy đơn hàng", HttpStatus.NOT_FOUND));

        Shop shop = order.getShop();
        User student = order.getUser();
        boolean isStudent = student.getId().equals(currentUser.getId());
        boolean isVendor = shop.getOwner() != null && shop.getOwner().getId().equals(currentUser.getId());
        if (!isStudent && !isVendor) {
            throw new AppException("Bạn không có quyền truy cập phòng chat của đơn này", HttpStatus.FORBIDDEN);
        }

        ChatRoom room = chatRoomRepository.findByStudentIdAndShopId(student.getId(), shop.getId())
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder()
                        .student(student)
                        .shop(shop)
                        .build()));

        return buildChatRoomResponse(room, currentUser);
    }

    @Transactional
    public ChatMessageResponse processMessage(SendMessageRequest request, String senderPhone) {
        User sender = getCurrentUser(senderPhone);
        ChatRoom room;

        if (request.getRoomId() != null) {
            room = chatRoomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phòng chat", HttpStatus.NOT_FOUND));
            validateUserInRoom(room, sender);
        } else if (request.getShopId() != null) {
            Shop shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> new AppException("Không tìm thấy quán ăn", HttpStatus.NOT_FOUND));
            room = chatRoomRepository.findByStudentIdAndShopId(sender.getId(), shop.getId())
                    .orElseGet(() -> chatRoomRepository.save(ChatRoom.builder().student(sender).shop(shop).build()));
        } else {
            throw new AppException("Phải cung cấp roomId hoặc shopId", HttpStatus.BAD_REQUEST);
        }

        // Lưu tin nhắn
        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .content(request.getContent())
                .build();
        message = messageRepository.save(message);

        // Cập nhật thời gian phòng chat
        room.setUpdatedAt(LocalDateTime.now());

        // Auto-read cho người gửi
        if (room.getStudent().getId().equals(sender.getId())) {
            room.setStudentLastReadAt(LocalDateTime.now());
        } else {
            room.setShopLastReadAt(LocalDateTime.now());
        }
        chatRoomRepository.save(room);

        // Chuyển sang DTO
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(room.getId())
                .senderId(sender.getId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();

        // Broadcast qua WebSocket
        messagingTemplate.convertAndSend("/topic/chat/" + room.getId(), response);

        // Bắn Notification Offline
        User receiver = sender.getId().equals(room.getStudent().getId()) ? room.getShop().getOwner() : room.getStudent();
        notificationService.notifyUser(receiver, "Tin nhắn từ " + sender.getFullName(), message.getContent(), "CHAT", room.getId());

        return response; // Trả về DTO cho FE (Option 3)
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(UUID roomId, String userPhone) {
        User currentUser = getCurrentUser(userPhone);
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng chat", HttpStatus.NOT_FOUND));
        validateUserInRoom(room, currentUser);

        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(m -> ChatMessageResponse.builder()
                        .id(m.getId())
                        .roomId(roomId)
                        .senderId(m.getSender().getId())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getUserChatRooms(String userPhone) {
        User currentUser = getCurrentUser(userPhone);
        String role = currentUser.getRole().name();

        Iterable<ChatRoom> rooms = ("VENDOR".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role))
                ? chatRoomRepository.findAllByVendorId(currentUser.getId())
                : chatRoomRepository.findAllByStudentId(currentUser.getId());

        List<ChatRoomResponse> responses = new ArrayList<>();
        for (ChatRoom room : rooms) {
            responses.add(buildChatRoomResponse(room, currentUser));
        }
        return responses;
    }

    @Transactional
    public void markAsRead(UUID roomId, String userPhone) {
        User currentUser = getCurrentUser(userPhone);
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng chat", HttpStatus.NOT_FOUND));
        validateUserInRoom(room, currentUser);

        if (room.getStudent().getId().equals(currentUser.getId())) {
            room.setStudentLastReadAt(LocalDateTime.now());
        } else {
            room.setShopLastReadAt(LocalDateTime.now());
        }
        chatRoomRepository.save(room);
    }

    public long getUnreadCountByRoom(UUID roomId, String userPhone) {
        User currentUser = getCurrentUser(userPhone);
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException("Phòng chat không tồn tại", HttpStatus.NOT_FOUND));
        validateUserInRoom(room, currentUser);
        return calculateUnreadCount(room, currentUser);
    }

    public long getTotalUnreadCount(String userPhone) {
        return getUserChatRooms(userPhone).stream().mapToLong(ChatRoomResponse::getUnreadCount).sum();
    }

    // ---- HÀM TIỆN ÍCH BỔ TRỢ ----

    private ChatRoomResponse buildChatRoomResponse(ChatRoom room, User currentUser) {
        boolean isStudent = room.getStudent().getId().equals(currentUser.getId());

        Message lastMsg = messageRepository.findFirstByRoomIdOrderByCreatedAtDesc(room.getId()).orElse(null);
        long unreadCount = calculateUnreadCount(room, currentUser);

        return ChatRoomResponse.builder()
                .roomId(room.getId())
                .partnerId(isStudent ? room.getShop().getId() : room.getStudent().getId())
                .partnerName(isStudent ? room.getShop().getName() : room.getStudent().getFullName())
                .lastMessage(lastMsg != null ? lastMsg.getContent() : null)
                .lastMessageAt(lastMsg != null ? lastMsg.getCreatedAt() : null)
                .unreadCount(unreadCount)
                .build();
    }

    private long calculateUnreadCount(ChatRoom room, User currentUser) {
        boolean isStudent = room.getStudent().getId().equals(currentUser.getId());
        LocalDateTime lastReadAt = isStudent ? room.getStudentLastReadAt() : room.getShopLastReadAt();

        if (lastReadAt == null) {
            return messageRepository.countByRoomIdAndSenderIdNot(room.getId(), currentUser.getId());
        } else {
            return messageRepository.countByRoomIdAndSenderIdNotAndCreatedAtAfter(room.getId(), currentUser.getId(), lastReadAt);
        }
    }
}
