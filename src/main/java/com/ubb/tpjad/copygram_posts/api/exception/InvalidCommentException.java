package com.ubb.tpjad.copygram_posts.api.exception;

import lombok.Getter;

@Getter
public class InvalidCommentException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Invalid commentId %s";

    private final String commentId;

    public InvalidCommentException(String commentId) {
        super(MESSAGE_TEMPLATE.formatted(commentId));
        this.commentId = commentId;
    }
}
