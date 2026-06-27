UPDATE "orders"
SET "completed_at" = CURRENT_TIMESTAMP
WHERE "completed_at" IS NULL
  AND "status" IN ('COMPLETED', 'RECEIVED');
