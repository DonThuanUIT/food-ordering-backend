ALTER TABLE "users"
ALTER COLUMN "role" TYPE varchar(50) USING "role"::text;

ALTER TABLE "orders"
ALTER COLUMN "status" TYPE varchar(50) USING "status"::text;

ALTER TABLE "shops" ALTER COLUMN "status" DROP DEFAULT;
ALTER TABLE "shops" ALTER COLUMN "status" TYPE varchar(50) USING "status"::text;
ALTER TABLE "shops" ALTER COLUMN "status" SET DEFAULT 'PENDING';

DROP TYPE IF EXISTS "user_role";
DROP TYPE IF EXISTS "order_status";
DROP TYPE IF EXISTS "shop_status";