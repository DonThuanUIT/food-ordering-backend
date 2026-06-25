-- Update Shop 5 settings with real images
UPDATE "shop_settings" 
SET 
    "cover_url" = 'https://images.unsplash.com/photo-1552611052-33e04de081de?w=1200&auto=format&fit=crop&q=80',
    "logo_url" = 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=200&auto=format&fit=crop&q=80'
WHERE "shop_id" = '00000000-0000-0000-0000-000000000105';

-- Update existing foods of Shop 5
-- Mì Cay Cấp Độ 1 -> Mì Cay Hải Sản
UPDATE "foods"
SET 
    "name" = 'Mì Cay Hải Sản',
    "description" = 'Mì cay hải sản thập cẩm thơm ngon, sợi mì dai giòn kết hợp tôm tươi, mực ống, chả cá và rau nấm tươi mát.',
    "price" = 39000.00,
    "image_url" = 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=600&auto=format&fit=crop&q=80',
    "tags" = '["cay", "mon-nuoc", "hai-san", "mi-cay"]'::jsonb,
    "cuisine" = 'Hàn Quốc',
    "spicy_level" = 2
WHERE "id" = '00000000-0000-0000-0000-000000000318';

-- Mì Cay Cấp Độ 3 -> Mì Cay Bò Mỹ
UPDATE "foods"
SET 
    "name" = 'Mì Cay Bò Mỹ',
    "description" = 'Mì cay bò Mỹ lát mỏng mềm ngọt, nước dùng kim chi chua cay đậm đà kích thích vị giác.',
    "price" = 42000.00,
    "image_url" = 'https://images.unsplash.com/photo-1585032226651-759b368d7246?w=600&auto=format&fit=crop&q=80',
    "tags" = '["cay", "mon-nuoc", "thit-bo", "mi-cay"]'::jsonb,
    "cuisine" = 'Hàn Quốc',
    "spicy_level" = 3
WHERE "id" = '00000000-0000-0000-0000-000000000319';

-- Cơm Trộn Hàn Quốc (Bibimbap)
UPDATE "foods"
SET 
    "description" = 'Cơm trộn Bibimbap đầy đủ dinh dưỡng với thịt bò xào, trứng lòng đào, nấm kim châm, giá đỗ và sốt ớt chuông đặc trưng.',
    "price" = 45000.00,
    "image_url" = 'https://images.unsplash.com/photo-1512058564366-18510be2db19?w=600&auto=format&fit=crop&q=80',
    "tags" = '["com-tron", "mon-kho", "dinh-duong"]'::jsonb,
    "cuisine" = 'Hàn Quốc',
    "spicy_level" = 1
WHERE "id" = '00000000-0000-0000-0000-000000000320';

-- Kimbap Cơ Bản
UPDATE "foods"
SET 
    "name" = 'Kimbap Rong Biển',
    "description" = 'Kimbap cuộn rong biển nhân xá xíu, trứng chiên, cà rốt và dưa leo giòn mát ăn kèm sốt mè rang.',
    "price" = 29000.00,
    "image_url" = 'https://images.unsplash.com/photo-1534482421-64566f976cfa?w=600&auto=format&fit=crop&q=80',
    "tags" = '["kimbap", "mon-kho", "an-nhe"]'::jsonb,
    "cuisine" = 'Hàn Quốc',
    "spicy_level" = 0
WHERE "id" = '00000000-0000-0000-0000-000000000321';

-- Kimbap Phô Mai
UPDATE "foods"
SET 
    "name" = 'Kimbap Phô Mai Chiên Xù',
    "description" = 'Kimbap nhân phô mai kéo sợi chiên xù giòn tan béo ngậy bên trong, ăn kèm tương ớt sốt mayonnaise.',
    "price" = 35000.00,
    "image_url" = 'https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56?w=600&auto=format&fit=crop&q=80',
    "tags" = '["kimbap", "mon-ran", "pho-mai", "cay-nhe"]'::jsonb,
    "cuisine" = 'Hàn Quốc',
    "spicy_level" = 1
WHERE "id" = '00000000-0000-0000-0000-000000000322';

-- Add new tasty food items for Shop 5
INSERT INTO "foods" ("id", "shop_id", "category_id", "name", "description", "price", "image_url", "is_available", "tags", "cuisine", "spicy_level")
VALUES
    ('00000000-0000-0000-0000-000000000323', '00000000-0000-0000-0000-000000000105', '00000000-0000-0000-0000-000000000210', 'Bánh Gạo Cay Tokbokki', 'Bánh gạo Tokbokki dẻo dai xào cùng chả cá Hàn Quốc, bắp cải và sốt ớt cay nồng chuẩn vị Seoul.', 35000.00, 'https://images.unsplash.com/photo-1627308595229-7830a5c91f9f?w=600&auto=format&fit=crop&q=80', true, '["tokbokki", "mon-kho", "cay", "an-vat"]'::jsonb, 'Hàn Quốc', 2)
ON CONFLICT ("id") DO NOTHING;
