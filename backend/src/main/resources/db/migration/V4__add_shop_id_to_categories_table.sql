-- 1. Thêm cột shop_id
ALTER TABLE categories ADD COLUMN shop_id UUID;

-- 2. Tạo khóa ngoại
ALTER TABLE categories ADD CONSTRAINT fk_categories_shop FOREIGN KEY (shop_id) REFERENCES shops(id);

-- 3. Đảm bảo shop_id không được null
ALTER TABLE categories ALTER COLUMN shop_id SET NOT NULL;

-- 4. QUAN TRỌNG: Xóa ràng buộc Unique cũ của cột name (PostgreSQL thường đặt tên mặc định là bảng_cột_key)
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_name_key;

-- 5. Thêm ràng buộc Unique mới: (shop_id + name)
ALTER TABLE categories ADD CONSTRAINT uk_shop_category_name UNIQUE (shop_id, name);