ALTER TABLE "orders" ADD COLUMN "shipper_id" uuid REFERENCES "users"("id");
ALTER TABLE "orders" ADD COLUMN "shipper_latitude" double precision;
ALTER TABLE "orders" ADD COLUMN "shipper_longitude" double precision;
ALTER TABLE "orders" ADD COLUMN "shipper_location_updated_at" timestamp;
