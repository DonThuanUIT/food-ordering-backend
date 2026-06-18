-- Add new settings columns to shops
ALTER TABLE "shops" ADD COLUMN "cover_url" varchar(255);
ALTER TABLE "shops" ADD COLUMN "logo_url" varchar(255);
ALTER TABLE "shops" ADD COLUMN "is_open" boolean DEFAULT true;
ALTER TABLE "shops" ADD COLUMN "email" varchar(255);
ALTER TABLE "shops" ADD COLUMN "phone" varchar(255);
ALTER TABLE "shops" ADD COLUMN "bank_name" varchar(255);
ALTER TABLE "shops" ADD COLUMN "bank_account_number" varchar(255);
ALTER TABLE "shops" ADD COLUMN "bank_account_owner" varchar(255);
ALTER TABLE "shops" ADD COLUMN "order_alerts_enabled" boolean DEFAULT true;
ALTER TABLE "shops" ADD COLUMN "dorm_promotions_enabled" boolean DEFAULT true;
ALTER TABLE "shops" ADD COLUMN "turbo_mode_enabled" boolean DEFAULT false;
ALTER TABLE "shops" ADD COLUMN "mon_fri_open_time" time;
ALTER TABLE "shops" ADD COLUMN "mon_fri_close_time" time;
ALTER TABLE "shops" ADD COLUMN "sat_open_time" time;
ALTER TABLE "shops" ADD COLUMN "sat_close_time" time;
ALTER TABLE "shops" ADD COLUMN "sun_open_time" time;
ALTER TABLE "shops" ADD COLUMN "sun_close_time" time;

-- Add discount columns to orders
ALTER TABLE "orders" ADD COLUMN "voucher_code" varchar(255);
ALTER TABLE "orders" ADD COLUMN "discount_amount" decimal DEFAULT 0;

-- Create vouchers table
CREATE TABLE "vouchers" (
    "id" uuid PRIMARY KEY,
    "shop_id" uuid NOT NULL,
    "code" varchar(50) NOT NULL,
    "title" varchar(255) NOT NULL,
    "discount_type" varchar(50) NOT NULL, -- 'PERCENTAGE', 'FIXED_AMOUNT'
    "discount_value" decimal NOT NULL,
    "min_order_value" decimal DEFAULT 0,
    "max_discount_value" decimal,
    "apply_type" varchar(50) NOT NULL, -- 'ALL_MENU', 'SPECIFIC_FOODS'
    "start_date" timestamp,
    "end_date" timestamp,
    "is_active" boolean DEFAULT true,
    CONSTRAINT fk_voucher_shop FOREIGN KEY ("shop_id") REFERENCES "shops"("id") ON DELETE CASCADE
);

-- Create voucher_foods table for specific foods
CREATE TABLE "voucher_foods" (
    "voucher_id" uuid NOT NULL,
    "food_id" uuid NOT NULL,
    PRIMARY KEY ("voucher_id", "food_id"),
    CONSTRAINT fk_vf_voucher FOREIGN KEY ("voucher_id") REFERENCES "vouchers"("id") ON DELETE CASCADE,
    CONSTRAINT fk_vf_food FOREIGN KEY ("food_id") REFERENCES "foods"("id") ON DELETE CASCADE
);

-- Create index for voucher lookup
CREATE INDEX "idx_voucher_shop" ON "vouchers" ("shop_id");
CREATE INDEX "idx_voucher_code" ON "vouchers" ("code");
