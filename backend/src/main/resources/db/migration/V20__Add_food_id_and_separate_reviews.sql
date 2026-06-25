-- ============================================================
-- Flyway Migration V20: Add food_id and separate reviews
-- ============================================================

-- 1. Add food_id references to order_details table
ALTER TABLE "order_details"
    ADD COLUMN "food_id" uuid;

ALTER TABLE "order_details"
    ADD CONSTRAINT "fk_order_detail_food" FOREIGN KEY ("food_id") REFERENCES "foods" ("id") ON DELETE SET NULL;

-- 2. Create shop_reviews table
CREATE TABLE "shop_reviews" (
    "id" uuid PRIMARY KEY,
    "order_id" uuid UNIQUE NOT NULL,
    "user_id" uuid NOT NULL,
    "shop_id" uuid NOT NULL,
    "rating" integer NOT NULL CHECK ("rating" >= 1 AND "rating" <= 5),
    "comment" text,
    "created_at" timestamp NOT NULL,
    CONSTRAINT "fk_shop_review_order" FOREIGN KEY ("order_id") REFERENCES "orders" ("id") ON DELETE CASCADE,
    CONSTRAINT "fk_shop_review_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
    CONSTRAINT "fk_shop_review_shop" FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") ON DELETE CASCADE
);

CREATE INDEX "idx_shop_review_shop" ON "shop_reviews" ("shop_id");

-- 3. Create food_reviews table
CREATE TABLE "food_reviews" (
    "id" uuid PRIMARY KEY,
    "order_id" uuid NOT NULL,
    "user_id" uuid NOT NULL,
    "food_id" uuid NOT NULL,
    "rating" integer NOT NULL CHECK ("rating" >= 1 AND "rating" <= 5),
    "comment" text,
    "created_at" timestamp NOT NULL,
    CONSTRAINT "fk_food_review_order" FOREIGN KEY ("order_id") REFERENCES "orders" ("id") ON DELETE CASCADE,
    CONSTRAINT "fk_food_review_user" FOREIGN KEY ("user_id") REFERENCES "users" ("id") ON DELETE CASCADE,
    CONSTRAINT "fk_food_review_food" FOREIGN KEY ("food_id") REFERENCES "foods" ("id") ON DELETE CASCADE
);

CREATE INDEX "idx_food_review_food" ON "food_reviews" ("food_id");
CREATE INDEX "idx_food_review_order" ON "food_reviews" ("order_id");
