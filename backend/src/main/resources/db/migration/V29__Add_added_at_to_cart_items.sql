ALTER TABLE "cart_items"
    ADD COLUMN IF NOT EXISTS "added_at" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS "idx_cart_item_cart_added_at"
    ON "cart_items" ("cart_id", "added_at");
