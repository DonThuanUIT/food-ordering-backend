package com.foodorderingapp.backend.modules.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageRequest {
    private UUID shopId; // Nếu chưa có phòng chat, Mobile gửi shopId lên
    private UUID roomId; // Nếu đã có phòng chat rồi, Mobile gửi thẳng roomId lên

    @NotBlank(message = "Tin nhắn không được để trống")
    private String content;
}