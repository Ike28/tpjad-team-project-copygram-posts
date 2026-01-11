package com.ubb.tpjad.copygram_posts.api.exception;

import static com.ubb.tpjad.copygram_posts.api.exception.InvalidLikeActionException.COMMENT_ENTITY;
import static com.ubb.tpjad.copygram_posts.api.exception.InvalidLikeActionException.POST_ENTITY;

public class UnauthorizedDeletionException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "User %s is unauthorized to delete entity %s of type %s";

    private UnauthorizedDeletionException(String message) {
        super(message);
    }

    public static UnauthorizedDeletionException unauthorizedPostDelete(String userId, String postId) {
        return new UnauthorizedDeletionException(MESSAGE_TEMPLATE.formatted(userId, postId, POST_ENTITY));
    }

    public static UnauthorizedDeletionException unauthorizedCommentDelete(String userId, String postId) {
        return new UnauthorizedDeletionException(MESSAGE_TEMPLATE.formatted(userId, postId, COMMENT_ENTITY));
    }
}
