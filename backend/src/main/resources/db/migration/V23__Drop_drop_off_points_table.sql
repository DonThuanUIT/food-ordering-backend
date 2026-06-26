-- Xóa bảng drop_off_points và các cột liên quan
-- Lý do: Không còn sử dụng điểm giao hàng con, chỉ dùng tòa nhà

-- Xóa khóa ngoại trước
ALTER TABLE IF EXISTS "drop_off_points" DROP CONSTRAINT IF EXISTS "drop_off_points_building_id_fkey";
ALTER TABLE IF EXISTS "user_saved_addresses" DROP CONSTRAINT IF EXISTS "user_saved_addresses_drop_off_point_id_fkey";

-- Xóa cột drop_off_point_id trong user_saved_addresses (nếu bảng này vẫn còn)
ALTER TABLE IF EXISTS "user_saved_addresses" DROP COLUMN IF EXISTS "drop_off_point_id";

-- Xóa cột drop_off_snapshot trong orders
ALTER TABLE IF EXISTS "orders" DROP COLUMN IF EXISTS "drop_off_snapshot";

-- Xóa chỉ mục
DROP INDEX IF EXISTS "idx_dropoff_building";

-- Xóa bảng drop_off_points
DROP TABLE IF EXISTS "drop_off_points";