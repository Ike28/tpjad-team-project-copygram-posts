package com.ubb.tpjad.copygram_posts.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ubb.tpjad.copygram_posts.api.ToStringDeserializer;

public record RemoteAuthResult (
        boolean valid,
        @JsonProperty("user_id")
        @JsonDeserialize(using = ToStringDeserializer.class)
        String userId,
        String username
) {
    public static RemoteAuthResult invalid() {
        return new RemoteAuthResult(false, null, null);
    }
}
