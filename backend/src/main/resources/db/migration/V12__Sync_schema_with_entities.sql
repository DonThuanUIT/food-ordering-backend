-- ============================================================
-- Flyway Migration V12: Sync schema with Hibernate entities
-- ============================================================
-- Phát hiện: Entity Order.java có thêm trường cancel_reason
-- (String) nhưng chưa có trong bảng orders (V1 không có,
-- V7 chỉ thêm voucher_code + discount_amount).
-- Các Entity khác (User, Shop, Food, Cart, CartItem, OrderDetail,
-- Review, Building, DropOffPoint, BankAccount, Category,
-- ShopFollower, ShopSettings, Voucher) đã khớp với SQL.
-- ============================================================

ALTER TABLE "orders"
    ADD COLUMN "cancel_reason" varchar(255);