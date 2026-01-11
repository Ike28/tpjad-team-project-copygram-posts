package com.ubb.tpjad.copygram_posts.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record PostMetadataDto (
        @JsonProperty("post_id") String postId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("picture_id") String pictureId,
        @JsonProperty("description") String description,
        @JsonProperty("likes_count") long likesCount,
        @JsonProperty("comments") List<PostCommentDto> comments
) {
}
