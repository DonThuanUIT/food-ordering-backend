-- ============================================================
-- V10__Add_address_column_to_shops.sql
-- 
-- Sự cố: Entity Shop.java có trường address (với @Column(name = "address")),
-- nhưng khi tạo bảng "shops" ở V1__initial_schema.sql, cột "address"
-- đã bị thiếu. Các file migration V2 → V9 cũng không bổ sung.
--
-- Hậu quả: Khi chạy V10 (nay là V11) để seed test data, INSERT vào shops
-- bị lỗi: ERROR: column "address" of relation "shops" does not exist
--
-- Giải pháp: Thêm cột "address" kiểu VARCHAR(255) vào bảng "shops"
-- trước khi V11 (seed data) được chạy.
-- ============================================================

ALTER TABLE "shops"
ADD COLUMN "address" VARCHAR(255);