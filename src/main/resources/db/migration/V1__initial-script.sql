-- POSTS
CREATE TABLE post (
    id      CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    picture_id VARCHAR(512) NOT NULL,
    description VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- COMMENTS
CREATE TABLE comment (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    text VARCHAR(256) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id) REFERENCES post(id)
        ON DELETE CASCADE
);

-- POST_LIKE
CREATE TABLE post_like (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    post_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_post_like_post
        FOREIGN KEY (post_id) REFERENCES post(id)
        ON DELETE CASCADE,
    CONSTRAINT uk_post_like_user_post
        UNIQUE(user_id, post_id)
);

-- COMMENT_LIKE
CREATE TABLE comment_like (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    comment_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comment_like_comment
        FOREIGN KEY (comment_id) REFERENCES comment(id)
            ON DELETE CASCADE,
    CONSTRAINT uk_comment_like_user_comment
        UNIQUE(user_id, comment_id)
);

CREATE INDEX idx_post_like_post_id ON post_like(post_id);
CREATE INDEX idx_comment_like_comment_id ON comment_like(comment_id);
CREATE INDEX idx_comment_post_id ON comment(post_id);
