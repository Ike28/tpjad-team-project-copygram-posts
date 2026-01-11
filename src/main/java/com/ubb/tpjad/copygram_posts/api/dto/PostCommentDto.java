package com.ubb.tpjad.copygram_posts.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PostCommentDto (
        @JsonProperty("id") String id,
        @JsonProperty("post_id") String postId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("text") String text,
        @JsonProperty("likes_count") long likesCount
) {
}
