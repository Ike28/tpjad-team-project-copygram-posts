package com.ubb.tpjad.copygram_posts.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record PostDto (
        @JsonProperty("id") String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("picture_id") String pictureId,
        @JsonProperty("description") String description
) {
}
