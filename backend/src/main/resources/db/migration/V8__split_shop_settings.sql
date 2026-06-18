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

-- Copy existing settings data from shops to shop_settings (if any exists)
INSERT INTO "shop_settings" (
    "shop_id", "cover_url", "logo_url", "email", "phone",
    "bank_name", "bank_account_number", "bank_account_owner",
    "order_alerts_enabled", "dorm_promotions_enabled", "turbo_mode_enabled",
    "mon_fri_open_time", "mon_fri_close_time",
    "sat_open_time", "sat_close_time",
    "sun_open_time", "sun_close_time"
)
SELECT 
    "id", "cover_url", "logo_url", "email", "phone",
    "bank_name", "bank_account_number", "bank_account_owner",
    "order_alerts_enabled", "dorm_promotions_enabled", "turbo_mode_enabled",
    "mon_fri_open_time", "mon_fri_close_time",
    "sat_open_time", "sat_close_time",
    "sun_open_time", "sun_close_time"
FROM "shops";

-- Remove the redundant columns from the shops table
ALTER TABLE "shops" DROP COLUMN "cover_url";
ALTER TABLE "shops" DROP COLUMN "logo_url";
ALTER TABLE "shops" DROP COLUMN "email";
ALTER TABLE "shops" DROP COLUMN "phone";
ALTER TABLE "shops" DROP COLUMN "bank_name";
ALTER TABLE "shops" DROP COLUMN "bank_account_number";
ALTER TABLE "shops" DROP COLUMN "bank_account_owner";
ALTER TABLE "shops" DROP COLUMN "order_alerts_enabled";
ALTER TABLE "shops" DROP COLUMN "dorm_promotions_enabled";
ALTER TABLE "shops" DROP COLUMN "turbo_mode_enabled";
ALTER TABLE "shops" DROP COLUMN "mon_fri_open_time";
ALTER TABLE "shops" DROP COLUMN "mon_fri_close_time";
ALTER TABLE "shops" DROP COLUMN "sat_open_time";
ALTER TABLE "shops" DROP COLUMN "sat_close_time";
ALTER TABLE "shops" DROP COLUMN "sun_open_time";
ALTER TABLE "shops" DROP COLUMN "sun_close_time";
