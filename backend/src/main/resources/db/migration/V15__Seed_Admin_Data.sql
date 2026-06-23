-- ============================================================
-- V15__Seed_Admin_Data.sql
-- Data Seeding cho tài khoản Admin (2 tài khoản)
-- Idempotent: INSERT ... ON CONFLICT DO NOTHING
-- An toàn cho database dùng chung Neon
-- ============================================================

-- ============================================================
-- Bước 1: Seed Users (2 ADMIN)
-- Password: "123456" (BCrypt hash)
-- building_id = NULL, is_locked = false, is_email_verified = true
-- ============================================================
INSERT INTO "users" ("id", "phone", "password", "full_name", "role", "email", "is_email_verified", "is_locked", "building_id", "avatar_url", "created_at", "updated_at")
VALUES
    ('00000000-0000-0000-0000-000000000011', '0101111111', crypt('123456', gen_salt('bf', 10)), 'Quản Trị Viên 1', 'ADMIN', 'admin1@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000012', '0102222222', crypt('123456', gen_salt('bf', 10)), 'Quản Trị Viên 2', 'ADMIN', 'admin2@gmail.com', true, false, NULL, NULL, NOW(), NOW())
ON CONFLICT ("phone") DO NOTHING;