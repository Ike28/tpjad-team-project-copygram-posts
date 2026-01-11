package com.ubb.tpjad.copygram_posts.api.exception;

import lombok.Getter;

@Getter
public class InvalidPostException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Invalid postId %s";

    private final String postId;

    public InvalidPostException(String postId) {
        super(MESSAGE_TEMPLATE.formatted(postId));
        this.postId = postId;
    }
}
