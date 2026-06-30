CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    destination_id BIGINT NOT NULL,
    author_user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,

    CONSTRAINT chk_reviews_rating CHECK (rating BETWEEN 1 AND 5)
);