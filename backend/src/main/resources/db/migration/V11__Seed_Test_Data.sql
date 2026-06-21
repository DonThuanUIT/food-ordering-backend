-- ============================================================
-- V10__Seed_Test_Data.sql
-- Data Seeding cho môi trường test (5 Student + 5 Vendor)
-- Idempotent: INSERT ... ON CONFLICT DO NOTHING
-- An toàn cho database dùng chung Neon
-- ============================================================

-- ============================================================
-- Bước 0: Đảm bảo pgcrypto extension (dùng để hash mật khẩu)
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- Bước 1: Seed Users (5 STUDENT + 5 VENDOR)
-- Password: "123456" (BCrypt hash)
-- building_id = NULL (theo yêu cầu không seed Building)
-- ============================================================
-- STUDENT
INSERT INTO "users" ("id", "phone", "password", "full_name", "role", "email", "is_email_verified", "is_locked", "building_id", "avatar_url", "created_at", "updated_at")
VALUES
    ('00000000-0000-0000-0000-000000000001', '0111111111', crypt('123456', gen_salt('bf', 10)), 'Nguyễn Văn An', 'STUDENT', 'test1@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000002', '0122222222', crypt('123456', gen_salt('bf', 10)), 'Trần Thị Bình', 'STUDENT', 'test2@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000003', '0133333333', crypt('123456', gen_salt('bf', 10)), 'Lê Hoàng Cường', 'STUDENT', 'test3@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000004', '0144444444', crypt('123456', gen_salt('bf', 10)), 'Phạm Thị Dung', 'STUDENT', 'test4@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000005', '0155555555', crypt('123456', gen_salt('bf', 10)), 'Đỗ Minh Em', 'STUDENT', 'test5@gmail.com', true, false, NULL, NULL, NOW(), NOW())
ON CONFLICT ("phone") DO NOTHING;

-- VENDOR
INSERT INTO "users" ("id", "phone", "password", "full_name", "role", "email", "is_email_verified", "is_locked", "building_id", "avatar_url", "created_at", "updated_at")
VALUES
    ('00000000-0000-0000-0000-000000000006', '0166666666', crypt('123456', gen_salt('bf', 10)), 'Hoàng Văn Phúc', 'VENDOR', 'test6@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000007', '0177777777', crypt('123456', gen_salt('bf', 10)), 'Ngô Thị Hạnh', 'VENDOR', 'test7@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000008', '0188888888', crypt('123456', gen_salt('bf', 10)), 'Vũ Minh Tâm', 'VENDOR', 'test8@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000009', '0199999999', crypt('123456', gen_salt('bf', 10)), 'Đặng Thúy Kiều', 'VENDOR', 'test9@gmail.com', true, false, NULL, NULL, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000010', '0100000000', crypt('123456', gen_salt('bf', 10)), 'Bùi Quốc Bảo', 'VENDOR', 'test10@gmail.com', true, false, NULL, NULL, NOW(), NOW())
ON CONFLICT ("phone") DO NOTHING;

-- ============================================================
-- Bước 2: Seed Shops (5 shops - tất cả APPROVED + is_active = true)
-- ============================================================
INSERT INTO "shops" ("id", "owner_id", "name", "description", "address", "open_time", "close_time", "is_active", "status", "is_open")
VALUES
    ('00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000006', 'Trà Sữa KOI', 'Trà sữa Đài Loan chính gốc, topping đa dạng', 'Khuôn viên Đại học, Tòa A1', '07:00:00', '22:00:00', true, 'APPROVED', true),
    ('00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000007', 'Bánh Mì Que', 'Bánh mì que giòn rụm, sốt đặc biệt', 'Khuôn viên Đại học, Tòa B2', '06:30:00', '21:00:00', true, 'APPROVED', true),
    ('00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000008', 'Cơm Gà Xối Mỡ', 'Cơm gà xối mỡ giòn da, nước sốt thơm ngon', 'Khuôn viên Đại học, Tòa C3', '10:00:00', '20:30:00', true, 'APPROVED', true),
    ('00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000009', 'Phở Bò Hà Nội', 'Phở bò truyền thống, nước dùng ninh từ xương', 'Khuôn viên Đại học, Tòa D4', '06:00:00', '21:30:00', true, 'APPROVED', true),
    ('00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000010', 'Mì Cay Hàn Quốc', 'Mì cay Hàn Quốc, kim chi, cơm trộn', 'Khuôn viên Đại học, Tòa E5', '08:00:00', '22:30:00', true, 'APPROVED', true)
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 3: Seed ShopSettings (kết nối 1-1 với Shop)
-- ============================================================
INSERT INTO "shop_settings" ("shop_id", "cover_url", "logo_url", "is_open", "email", "phone", "bank_name", "bank_account_number", "bank_account_owner", "order_alerts_enabled", "dorm_promotions_enabled", "turbo_mode_enabled", "mon_fri_open_time", "mon_fri_close_time", "sat_open_time", "sat_close_time", "sun_open_time", "sun_close_time")
VALUES
    ('00000000-0000-0000-0000-000000000101', 'https://placehold.co/1200x400/FFB6C1/333?text=Tra+Sua+KOI', 'https://placehold.co/200x200/FF69B4/fff?text=KOI', true, 'koi@gmail.com', '0166666666', 'Vietcombank', '1234567890', 'Hoàng Văn Phúc', true, true, false, '07:00:00', '22:00:00', '07:00:00', '22:00:00', '08:00:00', '21:00:00'),
    ('00000000-0000-0000-0000-000000000102', 'https://placehold.co/1200x400/FFFACD/333?text=Banh+Mi+Que', 'https://placehold.co/200x200/FFD700/333?text=BMQ', true, 'banhmi@gmail.com', '0177777777', 'Techcombank', '2345678901', 'Ngô Thị Hạnh', true, true, false, '06:30:00', '21:00:00', '07:00:00', '21:00:00', '07:00:00', '20:00:00'),
    ('00000000-0000-0000-0000-000000000103', 'https://placehold.co/1200x400/FFE4B5/333?text=Com+Ga+Xoi+Mo', 'https://placehold.co/200x200/DEB887/333?text=CGXM', true, 'comga@gmail.com', '0188888888', 'BIDV', '3456789012', 'Vũ Minh Tâm', true, true, false, '10:00:00', '20:30:00', '10:00:00', '20:30:00', '10:00:00', '20:00:00'),
    ('00000000-0000-0000-0000-000000000104', 'https://placehold.co/1200x400/F5DEB3/333?text=Pho+Bo+Ha+Noi', 'https://placehold.co/200x200/D2691E/fff?text=PHO', true, 'phobo@gmail.com', '0199999999', 'Vietinbank', '4567890123', 'Đặng Thúy Kiều', true, true, false, '06:00:00', '21:30:00', '06:00:00', '21:30:00', '06:30:00', '21:00:00'),
    ('00000000-0000-0000-0000-000000000105', 'https://placehold.co/1200x400/FF6347/fff?text=Mi+Cay+Han+Quoc', 'https://placehold.co/200x200/DC143C/fff?text=MCHQ', true, 'micay@gmail.com', '0100000000', 'MB Bank', '5678901234', 'Bùi Quốc Bảo', true, true, true, '08:00:00', '22:30:00', '09:00:00', '22:30:00', '09:00:00', '22:00:00')
ON CONFLICT ("shop_id") DO NOTHING;

-- ============================================================
-- Bước 4: Seed Categories (2-3 category mỗi shop)
-- ============================================================
-- Shop 1: Trà Sữa KOI
INSERT INTO "categories" ("id", "shop_id", "name")
VALUES
    ('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000101', 'Trà Sữa'),
    ('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000101', 'Topping')
ON CONFLICT ("id") DO NOTHING;

-- Shop 2: Bánh Mì Que
INSERT INTO "categories" ("id", "shop_id", "name")
VALUES
    ('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000102', 'Bánh Mì Que'),
    ('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000102', 'Nước Uống')
ON CONFLICT ("id") DO NOTHING;

-- Shop 3: Cơm Gà Xối Mỡ
INSERT INTO "categories" ("id", "shop_id", "name")
VALUES
    ('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000103', 'Cơm Gà'),
    ('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000103', 'Thức Uống'),
    ('00000000-0000-0000-0000-000000000207', '00000000-0000-0000-0000-000000000103', 'Món Thêm')
ON CONFLICT ("id") DO NOTHING;

-- Shop 4: Phở Bò Hà Nội
INSERT INTO "categories" ("id", "shop_id", "name")
VALUES
    ('00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000104', 'Phở'),
    ('00000000-0000-0000-0000-000000000209', '00000000-0000-0000-0000-000000000104', 'Quẩy')
ON CONFLICT ("id") DO NOTHING;

-- Shop 5: Mì Cay Hàn Quốc
INSERT INTO "categories" ("id", "shop_id", "name")
VALUES
    ('00000000-0000-0000-0000-000000000210', '00000000-0000-0000-0000-000000000105', 'Mì Cay'),
    ('00000000-0000-0000-0000-000000000211', '00000000-0000-0000-0000-000000000105', 'Cơm Trộn'),
    ('00000000-0000-0000-0000-000000000212', '00000000-0000-0000-0000-000000000105', 'Kimbap')
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 5: Seed Foods (3-5 món mỗi shop)
-- ============================================================
-- Shop 1: Trà Sữa KOI
INSERT INTO "foods" ("id", "shop_id", "category_id", "name", "description", "price", "image_url", "is_available")
VALUES
    ('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Trà Sữa Trân Châu Hoàng Kim', 'Trà sữa vị caramel kết hợp trân châu hoàng kim', 35000.00, 'https://placehold.co/400x400/FFB6C1/333?text=Tra+Sua+TC+HK', true),
    ('00000000-0000-0000-0000-000000000302', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Trà Sữa Khoai Môn', 'Trà sữa khoai môn thơm béo', 40000.00, 'https://placehold.co/400x400/DDA0DD/333?text=TS+Khoai+Mon', true),
    ('00000000-0000-0000-0000-000000000303', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000201', 'Hồng Trà Chanh', 'Hồng trà tươi pha chanh mát lạnh', 25000.00, 'https://placehold.co/400x400/FFD700/333?text=Hong+Tra+Chanh', true),
    ('00000000-0000-0000-0000-000000000304', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000202', 'Trân Châu Đen', 'Trân châu đen dai ngon', 5000.00, 'https://placehold.co/400x400/333/fff?text=TC+Den', true),
    ('00000000-0000-0000-0000-000000000305', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000202', 'Pudding Trứng', 'Pudding trứng béo ngậy', 8000.00, 'https://placehold.co/400x400/FFFACD/333?text=Pudding', true)
ON CONFLICT ("id") DO NOTHING;

-- Shop 2: Bánh Mì Que
INSERT INTO "foods" ("id", "shop_id", "category_id", "name", "description", "price", "image_url", "is_available")
VALUES
    ('00000000-0000-0000-0000-000000000306', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203', 'Bánh Mì Que Pate - 2 Que', 'Bánh mì que pate truyền thống, 2 que/phần', 15000.00, 'https://placehold.co/400x400/DEB887/333?text=BMQ+Pate', true),
    ('00000000-0000-0000-0000-000000000307', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000203', 'Bánh Mì Que Xíu Mại - 2 Que', 'Bánh mì que xíu mại sốt cà', 20000.00, 'https://placehold.co/400x400/FF6347/fff?text=BMQ+Xiu+Mai', true),
    ('00000000-0000-0000-0000-000000000308', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000204', 'Nước Ngọt Coca Cola', 'Coca Cola lon 330ml', 10000.00, 'https://placehold.co/400x400/FF0000/fff?text=Coca', true),
    ('00000000-0000-0000-0000-000000000309', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000204', 'Nước Suối Lavie', 'Nước suối Lavie 500ml', 5000.00, 'https://placehold.co/400x400/87CEEB/333?text=Lavie', true)
ON CONFLICT ("id") DO NOTHING;

-- Shop 3: Cơm Gà Xối Mỡ
INSERT INTO "foods" ("id", "shop_id", "category_id", "name", "description", "price", "image_url", "is_available")
VALUES
    ('00000000-0000-0000-0000-000000000310', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000205', 'Cơm Gà Xối Mỡ - Cơ bản', 'Cơm gà xối mỡ giòn da, nước mắm gừng', 35000.00, 'https://placehold.co/400x400/FFE4B5/333?text=Com+Ga+XM', true),
    ('00000000-0000-0000-0000-000000000311', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000205', 'Cơm Gà Xối Mỡ - Đặc Biệt', 'Cơm gà xối mỡ thêm trứng ốp la + xúc xích', 45000.00, 'https://placehold.co/400x400/FFDAB9/333?text=Ga+DB', true),
    ('00000000-0000-0000-0000-000000000312', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000206', 'Trà Đá', 'Trà đá mát lạnh', 3000.00, 'https://placehold.co/400x400/8FBC8F/333?text=Tra+Da', true),
    ('00000000-0000-0000-0000-000000000313', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000207', 'Thêm Trứng Ốp La', 'Thêm 1 quả trứng ốp la', 5000.00, 'https://placehold.co/400x400/FFD700/333?text=Trung+Op+La', true),
    ('00000000-0000-0000-0000-000000000314', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000207', 'Thêm Xúc Xích', 'Thêm 1 cây xúc xích chiên', 7000.00, 'https://placehold.co/400x400/DC143C/fff?text=Xuc+Xich', true)
ON CONFLICT ("id") DO NOTHING;

-- Shop 4: Phở Bò Hà Nội
INSERT INTO "foods" ("id", "shop_id", "category_id", "name", "description", "price", "image_url", "is_available")
VALUES
    ('00000000-0000-0000-0000-000000000315', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000208', 'Phở Bò Tái', 'Phở bò tái chín, nước dùng thanh ngọt', 40000.00, 'https://placehold.co/400x400/F5DEB3/333?text=Pho+Bo+Tai', true),
    ('00000000-0000-0000-0000-000000000316', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000208', 'Phở Bò Tái Nạm Gầu', 'Phở bò đủ loại: tái, nạm, gầu', 50000.00, 'https://placehold.co/400x400/FFDAB9/333?text=Pho+TN+Nam+Gau', true),
    ('00000000-0000-0000-0000-000000000317', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000209', 'Quẩy (1 cây)', 'Quẩy giòn rụm, chấm phở', 5000.00, 'https://placehold.co/400x400/FFD700/333?text=Quay', true)
ON CONFLICT ("id") DO NOTHING;

-- Shop 5: Mì Cay Hàn Quốc
INSERT INTO "foods" ("id", "shop_id", "category_id", "name", "description", "price", "image_url", "is_available")
VALUES
    ('00000000-0000-0000-0000-000000000318', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000210', 'Mì Cay Cấp Độ 1', 'Mì cay Hàn Quốc - cấp độ 1 (dễ ăn)', 30000.00, 'https://placehold.co/400x400/FF6347/fff?text=Mi+Cay+1', true),
    ('00000000-0000-0000-0000-000000000319', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000210', 'Mì Cay Cấp Độ 3', 'Mì cay Hàn Quốc - cấp độ 3 (dành cho dân nghiện cay)', 35000.00, 'https://placehold.co/400x400/DC143C/fff?text=Mi+Cay+3', true),
    ('00000000-0000-0000-0000-000000000320', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000211', 'Cơm Trộn Hàn Quốc (Bibimbap)', 'Cơm trộn Hàn Quốc với rau củ, thịt bò và trứng', 45000.00, 'https://placehold.co/400x400/FF8C00/fff?text=Bibimbap', true),
    ('00000000-0000-0000-0000-000000000321', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000212', 'Kimbap Cơ Bản', 'Kimbap Hàn Quốc nhân cơ bản', 25000.00, 'https://placehold.co/400x400/556B2F/fff?text=Kimbap', true),
    ('00000000-0000-0000-0000-000000000322', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000212', 'Kimbap Phô Mai', 'Kimbap nhân phô mai béo ngậy', 30000.00, 'https://placehold.co/400x400/FFD700/333?text=Kimbap+PM', true)
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 6: Seed BankAccounts (mỗi Vendor 1 tài khoản ngân hàng)
-- ============================================================
INSERT INTO "bank_accounts" ("id", "owner_id", "bank_name", "account_number", "account_owner", "qr_code_url", "is_default")
VALUES
    ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000006', 'Vietcombank', '1012345678', 'Hoàng Văn Phúc', 'https://placehold.co/200x200/fff/333?text=QR+KOI', true),
    ('00000000-0000-0000-0000-000000000402', '00000000-0000-0000-0000-000000000007', 'Techcombank', '1023456789', 'Ngô Thị Hạnh', 'https://placehold.co/200x200/fff/333?text=QR+BMQ', true),
    ('00000000-0000-0000-0000-000000000403', '00000000-0000-0000-0000-000000000008', 'BIDV', '1034567890', 'Vũ Minh Tâm', 'https://placehold.co/200x200/fff/333?text=QR+CGXM', true),
    ('00000000-0000-0000-0000-000000000404', '00000000-0000-0000-0000-000000000009', 'Vietinbank', '1045678901', 'Đặng Thúy Kiều', 'https://placehold.co/200x200/fff/333?text=QR+PHO', true),
    ('00000000-0000-0000-0000-000000000405', '00000000-0000-0000-0000-000000000010', 'MB Bank', '1056789012', 'Bùi Quốc Bảo', 'https://placehold.co/200x200/fff/333?text=QR+MCHQ', true)
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 7: Seed Carts (mỗi Student 1 giỏ hàng)
-- ============================================================
INSERT INTO "carts" ("id", "user_id")
VALUES
    ('00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000001'),
    ('00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000002'),
    ('00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000003'),
    ('00000000-0000-0000-0000-000000000504', '00000000-0000-0000-0000-000000000004'),
    ('00000000-0000-0000-0000-000000000505', '00000000-0000-0000-0000-000000000005')
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 8: Seed CartItems (vài item mẫu trong giỏ hàng)
-- ============================================================
INSERT INTO "cart_items" ("id", "cart_id", "food_id", "quantity", "note")
VALUES
    -- Student 1: 2 ly trà sữa KOI
    ('00000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000301', 2, 'Ít đá, ít đường'),
    ('00000000-0000-0000-0000-000000000602', '00000000-0000-0000-0000-000000000501', '00000000-0000-0000-0000-000000000304', 1, 'Thêm trân châu'),
    -- Student 2: bánh mì que
    ('00000000-0000-0000-0000-000000000603', '00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000306', 3, 'Kẹp thêm pate'),
    ('00000000-0000-0000-0000-000000000604', '00000000-0000-0000-0000-000000000502', '00000000-0000-0000-0000-000000000308', 1, 'Lạnh'),
    -- Student 3: cơm gà
    ('00000000-0000-0000-0000-000000000605', '00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000311', 1, ''),
    ('00000000-0000-0000-0000-000000000606', '00000000-0000-0000-0000-000000000503', '00000000-0000-0000-0000-000000000312', 1, '')
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 9: Seed Vouchers (1-2 voucher mỗi shop)
-- ============================================================
INSERT INTO "vouchers" ("id", "shop_id", "code", "title", "discount_type", "discount_value", "min_order_value", "max_discount_value", "apply_type", "start_date", "end_date", "is_active")
VALUES
    -- Shop 1: Trà Sữa KOI
    ('00000000-0000-0000-0000-000000000701', '00000000-0000-0000-0000-000000000101', 'KOI10', 'Giảm 10% đơn hàng', 'PERCENTAGE', 10, 50000, 15000, 'ALL_MENU', NOW(), NOW() + INTERVAL '30 days', true),
    ('00000000-0000-0000-0000-000000000702', '00000000-0000-0000-0000-000000000101', 'KOIFREE', 'Free topping trân châu', 'FIXED_AMOUNT', 5000, 30000, 5000, 'ALL_MENU', NOW(), NOW() + INTERVAL '14 days', true),
    -- Shop 2: Bánh Mì Que
    ('00000000-0000-0000-0000-000000000703', '00000000-0000-0000-0000-000000000102', 'BANHMI20', 'Giảm 20% đơn bánh mì', 'PERCENTAGE', 20, 20000, 10000, 'ALL_MENU', NOW(), NOW() + INTERVAL '30 days', true),
    -- Shop 5: Mì Cay Hàn Quốc
    ('00000000-0000-0000-0000-000000000704', '00000000-0000-0000-0000-000000000105', 'CAY15', 'Giảm 15k đơn mì cay', 'FIXED_AMOUNT', 15000, 50000, 15000, 'ALL_MENU', NOW(), NOW() + INTERVAL '7 days', true)
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 10: Seed Orders (vài đơn hàng test)
-- ============================================================
INSERT INTO "orders" ("id", "user_id", "shop_id", "bank_account_id", "total_price", "status", "payment_proof_url", "building_snapshot", "drop_off_snapshot", "voucher_code", "discount_amount", "created_at")
VALUES
    -- Order 1: Student 1 mua trà sữa KOI
    ('00000000-0000-0000-0000-000000000801', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101', '00000000-0000-0000-0000-000000000401', 80000.00, 'RECEIVED', NULL, 'Ký túc xá', 'Phòng 101', NULL, 0, NOW() - INTERVAL '3 days'),
    -- Order 2: Student 2 mua bánh mì que
    ('00000000-0000-0000-0000-000000000802', '00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000102', '00000000-0000-0000-0000-000000000402', 55000.00, 'DELIVERING', NULL, 'Ký túc xá', 'Phòng 202', 'BANHMI20', 11000, NOW() - INTERVAL '1 day'),
    -- Order 3: Student 3 mua cơm gà (đã hủy)
    ('00000000-0000-0000-0000-000000000803', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000103', '00000000-0000-0000-0000-000000000403', 48000.00, 'FAILED', NULL, 'Ký túc xá', 'Phòng 303', NULL, 0, NOW() - INTERVAL '5 hours'),
    -- Order 4: Student 1 mua thêm phở (PENDING - chờ xử lý)
    ('00000000-0000-0000-0000-000000000804', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000104', '00000000-0000-0000-0000-000000000404', 45000.00, 'PENDING', NULL, NULL, NULL, NULL, 0, NOW()),
    -- Order 5: Student 4 mua mì cay (CONFIRMED)
    ('00000000-0000-0000-0000-000000000805', '00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000405', 65000.00, 'CONFIRMED', NULL, NULL, NULL, 'CAY15', 15000, NOW())
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 11: Seed OrderDetails
-- ============================================================
INSERT INTO "order_details" ("id", "order_id", "food_name_snapshot", "price_snapshot", "quantity")
VALUES
    -- Order 1: 2 ly trà sữa + 1 topping
    ('00000000-0000-0000-0000-000000000901', '00000000-0000-0000-0000-000000000801', 'Trà Sữa Trân Châu Hoàng Kim', 35000.00, 2),
    ('00000000-0000-0000-0000-000000000902', '00000000-0000-0000-0000-000000000801', 'Trân Châu Đen', 5000.00, 2),
    -- Order 2: 3 bánh mì que + 1 coca
    ('00000000-0000-0000-0000-000000000903', '00000000-0000-0000-0000-000000000802', 'Bánh Mì Que Pate - 2 Que', 15000.00, 3),
    ('00000000-0000-0000-0000-000000000904', '00000000-0000-0000-0000-000000000802', 'Nước Ngọt Coca Cola', 10000.00, 1),
    -- Order 3: 1 cơm gà đặc biệt + 1 trà đá (đã hủy)
    ('00000000-0000-0000-0000-000000000905', '00000000-0000-0000-0000-000000000803', 'Cơm Gà Xối Mỡ - Đặc Biệt', 45000.00, 1),
    ('00000000-0000-0000-0000-000000000906', '00000000-0000-0000-0000-000000000803', 'Trà Đá', 3000.00, 1),
    -- Order 4: 1 phở bò tái (PENDING)
    ('00000000-0000-0000-0000-000000000907', '00000000-0000-0000-0000-000000000804', 'Phở Bò Tái', 40000.00, 1),
    ('00000000-0000-0000-0000-000000000908', '00000000-0000-0000-0000-000000000804', 'Quẩy (1 cây)', 5000.00, 1),
    -- Order 5: 1 mì cay + 1 kimbap (CONFIRMED)
    ('00000000-0000-0000-0000-000000000909', '00000000-0000-0000-0000-000000000805', 'Mì Cay Cấp Độ 1', 30000.00, 1),
    ('00000000-0000-0000-0000-000000000910', '00000000-0000-0000-0000-000000000805', 'Kimbap Phô Mai', 30000.00, 1)
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 12: Seed Reviews (cho các đơn đã RECEIVED)
-- ============================================================
INSERT INTO "reviews" ("id", "order_id", "user_id", "rating", "comment", "created_at")
VALUES
    ('00000000-0000-0000-0000-000000001001', '00000000-0000-0000-0000-000000000801', '00000000-0000-0000-0000-000000000001', 5, 'Trà sữa ngon, giao hàng nhanh! Sẽ ủng hộ tiếp.', NOW() - INTERVAL '2 days')
ON CONFLICT ("id") DO NOTHING;

-- ============================================================
-- Bước 13: Seed ShopFollowers
-- ============================================================
INSERT INTO "shop_followers" ("id", "user_id", "shop_id")
VALUES
    ('00000000-0000-0000-0000-000000001101', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000101'),
    ('00000000-0000-0000-0000-000000001102', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000105'),
    ('00000000-0000-0000-0000-000000001103', '00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000102'),
    ('00000000-0000-0000-0000-000000001104', '00000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000103'),
    ('00000000-0000-0000-0000-000000001105', '00000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000104'),
    ('00000000-0000-0000-0000-000000001106', '00000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000105')
ON CONFLICT ("id") DO NOTHING;