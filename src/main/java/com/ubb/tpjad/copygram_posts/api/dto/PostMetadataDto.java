package com.ubb.tpjad.copygram_posts.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PostMetadataDto (
        @JsonProperty("post_id") String postId,
        @JsonProperty("likes_count") long likesCount,
        @JsonProperty("comments") List<PostCommentDto> comments
) {
}
