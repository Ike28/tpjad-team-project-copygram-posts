package com.ubb.tpjad.copygram_posts.api.exception;

import lombok.Getter;

@Getter
public class InvalidLikeActionException extends RuntimeException {
    public static final String POST_ENTITY = "POST";
    public static final String COMMENT_ENTITY = "COMMENT";

    private static final String DUPLICATE_LIKE_MESSAGE_TEMPLATE = "Duplicate like request for entity %s of type %s from user %s";
    private static final String INVALID_UNLIKE_MESSAGE_TEMPLATE = "Invalid unlike request for entity %s of type %s from user %s";

    private InvalidLikeActionException(String message) {
        super(message);
    }

    public static InvalidLikeActionException duplicatePostLike(String postId, String userId) {
        return new InvalidLikeActionException(DUPLICATE_LIKE_MESSAGE_TEMPLATE.formatted(postId, POST_ENTITY, userId));
    }

    public static InvalidLikeActionException duplicateCommentLike(String commentId, String userId) {
        return new InvalidLikeActionException(DUPLICATE_LIKE_MESSAGE_TEMPLATE.formatted(commentId, COMMENT_ENTITY, userId));
    }

    public static InvalidLikeActionException invalidPostUnlike(String postId, String userId) {
        return new InvalidLikeActionException(INVALID_UNLIKE_MESSAGE_TEMPLATE.formatted(postId, POST_ENTITY, userId));
    }

    public static InvalidLikeActionException invalidCommentUnlike(String commentId, String userId) {
        return new InvalidLikeActionException(INVALID_UNLIKE_MESSAGE_TEMPLATE.formatted(commentId, COMMENT_ENTITY, userId));
    }
}
