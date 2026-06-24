-- ============================================================
-- V16__Update_Chat_Read_Receipt_Columns.sql
-- ============================================================

-- 1. Thêm 2 cột mốc thời gian đọc vào bảng chat_rooms
ALTER TABLE "chat_rooms"
    ADD COLUMN "student_last_read_at" TIMESTAMP,
    ADD COLUMN "shop_last_read_at" TIMESTAMP;

-- 2. Xóa cột is_read cũ ở bảng messages (Tối ưu hóa Option 2)
ALTER TABLE "messages"
DROP COLUMN "is_read";