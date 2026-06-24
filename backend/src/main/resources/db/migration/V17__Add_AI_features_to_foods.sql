-- V16: Add AI features to foods table for automated analysis and recommendation
ALTER TABLE foods 
ADD COLUMN tags JSONB,
ADD COLUMN cuisine VARCHAR(50),
ADD COLUMN spicy_level INT DEFAULT 0;
