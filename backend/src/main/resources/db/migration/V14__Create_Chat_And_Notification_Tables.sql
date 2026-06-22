-- ============================================================
-- V14__Create_Chat_And_Notification_Tables.sql
-- ============================================================

-- 1. Bảng thiết bị người dùng (Hỗ trợ nhiều thiết bị / 1 user)
CREATE TABLE "user_devices" (
                                "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                "user_id" UUID NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
                                "fcm_token" VARCHAR(255) NOT NULL UNIQUE,
                                "device_info" VARCHAR(255), -- Ví dụ: "Samsung S23" hoặc "iPhone 15"
                                "created_at" TIMESTAMP DEFAULT NOW(),
                                "last_active_at" TIMESTAMP DEFAULT NOW()
);

-- 2. Bảng Phòng Chat (1 Sinh viên - 1 Quán)
CREATE TABLE "chat_rooms" (
                              "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              "student_id" UUID NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
                              "shop_id" UUID NOT NULL REFERENCES "shops"("id") ON DELETE CASCADE,
                              "created_at" TIMESTAMP DEFAULT NOW(),
                              "updated_at" TIMESTAMP DEFAULT NOW(),
                              UNIQUE("student_id", "shop_id") -- Đảm bảo không tạo 2 phòng chat trùng lặp
);

-- 3. Bảng Tin nhắn
CREATE TABLE "messages" (
                            "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            "room_id" UUID NOT NULL REFERENCES "chat_rooms"("id") ON DELETE CASCADE,
                            "sender_id" UUID NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
                            "content" TEXT NOT NULL,
                            "is_read" BOOLEAN DEFAULT FALSE,
                            "created_at" TIMESTAMP DEFAULT NOW()
);

-- 4. Bảng Lịch sử Thông báo
CREATE TABLE "notifications" (
                                 "id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 "user_id" UUID NOT NULL REFERENCES "users"("id") ON DELETE CASCADE,
                                 "title" VARCHAR(255) NOT NULL,
                                 "body" TEXT NOT NULL,
                                 "type" VARCHAR(50) NOT NULL, -- Ví dụ: 'CHAT', 'ORDER_STATUS', 'SYSTEM'
                                 "reference_id" UUID, -- Trỏ tới order_id hoặc room_id tùy thuộc vào type
                                 "is_read" BOOLEAN DEFAULT FALSE,
                                 "created_at" TIMESTAMP DEFAULT NOW()
);

-- Tạo Index để truy vấn nhanh (Rất quan trọng cho hiệu năng)
CREATE INDEX "idx_user_devices_userid" ON "user_devices"("user_id");
CREATE INDEX "idx_messages_roomid" ON "messages"("room_id");
CREATE INDEX "idx_notifications_userid" ON "notifications"("user_id");