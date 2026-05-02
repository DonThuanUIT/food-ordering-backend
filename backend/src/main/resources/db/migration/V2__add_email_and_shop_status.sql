CREATE TYPE "shop_status" AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'BANNED');

ALTER TABLE "shops" ADD COLUMN "status" shop_status DEFAULT 'PENDING';

ALTER TABLE "users" ADD COLUMN "email" varchar UNIQUE;
ALTER TABLE "users" ADD COLUMN "is_email_verified" boolean DEFAULT false;

CREATE INDEX "idx_user_email" ON "users" ("email");