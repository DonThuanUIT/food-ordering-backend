ALTER TABLE "orders"
    ADD COLUMN IF NOT EXISTS "completed_at" timestamp;

UPDATE "orders"
SET "completed_at" = "created_at"
WHERE "completed_at" IS NULL
  AND "status" IN ('COMPLETED', 'RECEIVED');

CREATE INDEX IF NOT EXISTS "idx_order_completed"
    ON "orders" ("completed_at");
