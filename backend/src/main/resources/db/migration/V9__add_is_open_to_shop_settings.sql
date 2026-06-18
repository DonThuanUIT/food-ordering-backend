-- Add is_open column to shop_settings table
ALTER TABLE "shop_settings" ADD COLUMN "is_open" boolean DEFAULT true;

-- Sync is_open from shops to shop_settings
UPDATE "shop_settings" ss
SET "is_open" = s.is_open
FROM "shops" s
WHERE ss.shop_id = s.id;
