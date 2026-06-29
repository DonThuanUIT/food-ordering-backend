CREATE TABLE review_replies (
    id UUID PRIMARY KEY,
    review_id UUID NOT NULL,
    review_type VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reply_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_review_replies_review ON review_replies(review_id);
