package com.ubb.tpjad.copygram_posts.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostDto (
        @JsonProperty("id") String id,
        @JsonProperty("user_id") String userId,
        @JsonProperty("picture_id") String pictureId,
        @JsonProperty("description") String description,
        @JsonProperty("metadata") PostMetadataDto metadata
) {
}
