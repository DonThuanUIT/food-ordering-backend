-- Add is_open column to shops
ALTER TABLE "shops" ADD COLUMN "is_open" boolean DEFAULT true;

-- Add discount columns to orders
ALTER TABLE "orders" ADD COLUMN "voucher_code" varchar(255);
ALTER TABLE "orders" ADD COLUMN "discount_amount" decimal DEFAULT 0;

-- Create shop_settings table
CREATE TABLE "shop_settings" (
    "shop_id" uuid PRIMARY KEY,
    "cover_url" varchar(255),
    "logo_url" varchar(255),
    "email" varchar(255),
    "phone" varchar(255),
    "bank_name" varchar(255),
    "bank_account_number" varchar(255),
    "bank_account_owner" varchar(255),
    "order_alerts_enabled" boolean DEFAULT true,
    "dorm_promotions_enabled" boolean DEFAULT true,
    "turbo_mode_enabled" boolean DEFAULT false,
    "mon_fri_open_time" time,
    "mon_fri_close_time" time,
    "sat_open_time" time,
    "sat_close_time" time,
    "sun_open_time" time,
    "sun_close_time" time,
    CONSTRAINT fk_settings_shop FOREIGN KEY ("shop_id") REFERENCES "shops"("id") ON DELETE CASCADE
);

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
