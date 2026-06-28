UPDATE "shops" s
SET
    "is_active" = false,
    "is_open" = false
FROM "users" u
WHERE s."owner_id" = u."id"
  AND (u."is_locked" = true OR s."status" IN ('BANNED', 'REJECTED'));

UPDATE "shop_settings" ss
SET "is_open" = false
FROM "shops" s
JOIN "users" u ON s."owner_id" = u."id"
WHERE ss."shop_id" = s."id"
  AND (u."is_locked" = true OR s."status" IN ('BANNED', 'REJECTED'));
