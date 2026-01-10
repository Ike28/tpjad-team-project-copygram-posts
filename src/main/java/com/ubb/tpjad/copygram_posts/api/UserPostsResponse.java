package com.ubb.tpjad.copygram_posts.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;

import java.util.List;

public record UserPostsResponse (
        @JsonProperty("user_id") String userId,
        @JsonProperty("posts") List<PostDto> posts
) {
}
