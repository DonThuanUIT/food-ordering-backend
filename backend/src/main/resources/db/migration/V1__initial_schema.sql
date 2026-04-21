CREATE TYPE "user_role" AS ENUM (
  'STUDENT',
  'VENDOR',
  'ADMIN'
);

CREATE TYPE "order_status" AS ENUM (
  'PENDING',
  'CONFIRMED',
  'DELIVERING',
  'RECEIVED',
  'FAILED',
  'REJECTED'
);

CREATE TABLE "buildings" (
                             "id" uuid PRIMARY KEY,
                             "name" varchar UNIQUE NOT NULL
);

CREATE TABLE "drop_off_points" (
                                   "id" uuid PRIMARY KEY,
                                   "building_id" uuid,
                                   "name" varchar NOT NULL
);

CREATE TABLE "users" (
                         "id" uuid PRIMARY KEY,
                         "phone" varchar UNIQUE NOT NULL,
                         "password" varchar NOT NULL,
                         "full_name" varchar NOT NULL,
                         "role" user_role NOT NULL,
                         "building_id" uuid,
                         "created_at" timestamp,
                         "updated_at" timestamp
);

CREATE TABLE "user_saved_addresses" (
                                        "id" uuid PRIMARY KEY,
                                        "user_id" uuid,
                                        "building_id" uuid,
                                        "drop_off_point_id" uuid,
                                        "is_default" boolean DEFAULT false
);

CREATE TABLE "bank_accounts" (
                                 "id" uuid PRIMARY KEY,
                                 "owner_id" uuid,
                                 "bank_name" varchar NOT NULL,
                                 "account_number" varchar NOT NULL,
                                 "account_owner" varchar NOT NULL,
                                 "qr_code_url" varchar NOT NULL,
                                 "is_default" boolean DEFAULT false
);

CREATE TABLE "shops" (
                         "id" uuid PRIMARY KEY,
                         "owner_id" uuid,
                         "name" varchar NOT NULL,
                         "description" text,
                         "open_time" time,
                         "close_time" time,
                         "is_active" boolean DEFAULT true
);

CREATE TABLE "categories" (
                              "id" uuid PRIMARY KEY,
                              "name" varchar UNIQUE NOT NULL
);

CREATE TABLE "foods" (
                         "id" uuid PRIMARY KEY,
                         "shop_id" uuid,
                         "category_id" uuid,
                         "name" varchar NOT NULL,
                         "price" decimal NOT NULL,
                         "image_url" varchar,
                         "description" text,
                         "is_available" boolean DEFAULT true
);

CREATE TABLE "carts" (
                         "id" uuid PRIMARY KEY,
                         "user_id" uuid UNIQUE
);

CREATE TABLE "cart_items" (
                              "id" uuid PRIMARY KEY,
                              "cart_id" uuid,
                              "food_id" uuid,
                              "quantity" integer NOT NULL,
                              "note" varchar
);

CREATE TABLE "orders" (
                          "id" uuid PRIMARY KEY,
                          "user_id" uuid,
                          "shop_id" uuid,
                          "bank_account_id" uuid,
                          "total_price" decimal NOT NULL,
                          "status" order_status NOT NULL,
                          "payment_proof_url" varchar,
                          "building_snapshot" varchar,
                          "drop_off_snapshot" varchar,
                          "created_at" timestamp
);

CREATE TABLE "order_details" (
                                 "id" uuid PRIMARY KEY,
                                 "order_id" uuid,
                                 "food_name_snapshot" varchar NOT NULL,
                                 "price_snapshot" decimal NOT NULL,
                                 "quantity" integer NOT NULL
);

CREATE TABLE "reviews" (
                           "id" uuid PRIMARY KEY,
                           "order_id" uuid UNIQUE,
                           "user_id" uuid,
                           "rating" integer,
                           "comment" text,
                           "created_at" timestamp
);

CREATE TABLE "shop_followers" (
                                  "id" uuid PRIMARY KEY,
                                  "user_id" uuid,
                                  "shop_id" uuid
);

CREATE INDEX "idx_dropoff_building" ON "drop_off_points" ("building_id");

CREATE INDEX "idx_user_phone" ON "users" ("phone");

CREATE INDEX "idx_user_role" ON "users" ("role");

CREATE INDEX "idx_saved_address_user" ON "user_saved_addresses" ("user_id");

CREATE INDEX "idx_bank_owner" ON "bank_accounts" ("owner_id");

CREATE INDEX "idx_shop_owner" ON "shops" ("owner_id");

CREATE INDEX "idx_shop_active" ON "shops" ("is_active");

CREATE INDEX "idx_food_shop" ON "foods" ("shop_id");

CREATE INDEX "idx_food_category" ON "foods" ("category_id");

CREATE INDEX "idx_food_available" ON "foods" ("is_available");

CREATE INDEX "idx_cart_item_cart" ON "cart_items" ("cart_id");

CREATE INDEX "idx_order_user" ON "orders" ("user_id");

CREATE INDEX "idx_order_shop" ON "orders" ("shop_id");

CREATE INDEX "idx_order_status" ON "orders" ("status");

CREATE INDEX "idx_order_created" ON "orders" ("created_at");

CREATE INDEX "idx_order_detail_order" ON "order_details" ("order_id");

CREATE INDEX "idx_review_user" ON "reviews" ("user_id");

CREATE INDEX "idx_follower_composite" ON "shop_followers" ("user_id", "shop_id");

COMMENT ON COLUMN "reviews"."rating" IS '1-5 sao';

ALTER TABLE "drop_off_points" ADD FOREIGN KEY ("building_id") REFERENCES "buildings" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "users" ADD FOREIGN KEY ("building_id") REFERENCES "buildings" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "user_saved_addresses" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "user_saved_addresses" ADD FOREIGN KEY ("building_id") REFERENCES "buildings" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "user_saved_addresses" ADD FOREIGN KEY ("drop_off_point_id") REFERENCES "drop_off_points" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "bank_accounts" ADD FOREIGN KEY ("owner_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "shops" ADD FOREIGN KEY ("owner_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "foods" ADD FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "foods" ADD FOREIGN KEY ("category_id") REFERENCES "categories" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "carts" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "cart_items" ADD FOREIGN KEY ("cart_id") REFERENCES "carts" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "cart_items" ADD FOREIGN KEY ("food_id") REFERENCES "foods" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "orders" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "orders" ADD FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "orders" ADD FOREIGN KEY ("bank_account_id") REFERENCES "bank_accounts" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "order_details" ADD FOREIGN KEY ("order_id") REFERENCES "orders" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "reviews" ADD FOREIGN KEY ("order_id") REFERENCES "orders" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "reviews" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "shop_followers" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("id") DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "shop_followers" ADD FOREIGN KEY ("shop_id") REFERENCES "shops" ("id") DEFERRABLE INITIALLY IMMEDIATE;
