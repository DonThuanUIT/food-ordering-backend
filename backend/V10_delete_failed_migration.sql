-- ============================================================
-- Script dọn dẹp: Xóa lịch sử failed của V10
-- ============================================================
-- Lý do: V10__Seed_Test_Data.sql cũ đã failed vì thiếu cột address.
-- Sau khi tạo V10__Add_address_column_to_shops.sql (thêm cột address)
-- và đổi tên seed data thành V11, Flyway cần được xóa record failed
-- của version 10 để có thể chạy file V10 mới.
-- ============================================================
-- Chạy lệnh này thủ công trên DBeaver / DataGrip / psql
-- ============================================================

DELETE FROM flyway_schema_history
WHERE version = 10
  AND success = false;