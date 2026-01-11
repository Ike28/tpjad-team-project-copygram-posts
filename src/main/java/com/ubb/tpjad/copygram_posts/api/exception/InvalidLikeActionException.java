package com.ubb.tpjad.copygram_posts.api.exception;

import lombok.Getter;

@Getter
public class InvalidLikeActionException extends RuntimeException {
    private static final String DUPLICATE_LIKE_MESSAGE_TEMPLATE = "Duplicate like request for entity %s of type %s from user %s";
    private static final String INVALID_UNLIKE_MESSAGE_TEMPLATE = "Invalid unlike request for entity %s of type %s from user %s";
    private static final String POST_ENTITY = "POST";
    private static final String COMMENT_ENTITY = "POST";

    private final String userId;

    private InvalidLikeActionException(String message, String userId) {
        super(message);
        this.userId = userId;
    }

    public static InvalidLikeActionException duplicatePostLike(String postId, String userId) {
        return new InvalidLikeActionException(DUPLICATE_LIKE_MESSAGE_TEMPLATE.formatted(postId, POST_ENTITY, userId), userId);
    }

    public static InvalidLikeActionException duplicateCommentLike(String postId, String userId) {
        return new InvalidLikeActionException(DUPLICATE_LIKE_MESSAGE_TEMPLATE.formatted(postId, COMMENT_ENTITY, userId), userId);
    }

    public static InvalidLikeActionException invalidPostUnlike(String postId, String userId) {
        return new InvalidLikeActionException(INVALID_UNLIKE_MESSAGE_TEMPLATE.formatted(postId, POST_ENTITY, userId), userId);
    }

    public static InvalidLikeActionException invalidCommentUnlike(String postId, String userId) {
        return new InvalidLikeActionException(INVALID_UNLIKE_MESSAGE_TEMPLATE.formatted(postId, COMMENT_ENTITY, userId), userId);
    }
}
