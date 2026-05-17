-- Thêm cột is_locked với mặc định là false
ALTER TABLE "users" ADD COLUMN "is_locked" boolean DEFAULT false;

-- Cập nhật các user cũ (nếu có) để không bị lỗi NULL
UPDATE "users" SET "is_locked" = false WHERE "is_locked" IS NULL;