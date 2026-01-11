package com.ubb.tpjad.copygram_posts.api.exception;

import lombok.Getter;

@Getter
public class LimitExceededException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Retrieval limit of %s exceeded, requested limit is %s";

    private final int limit;

    public LimitExceededException(int requestedLimit, int allowedLimit) {
        super(MESSAGE_TEMPLATE.formatted(allowedLimit, requestedLimit));
        this.limit = allowedLimit;
    }
}
