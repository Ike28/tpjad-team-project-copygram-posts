package com.ubb.tpjad.copygram_posts.security;

public record RemoteAuthResult (
        boolean valid,
        String userId,
        String username
) {
    public static RemoteAuthResult invalid() {
        return new RemoteAuthResult(false, null, null);
    }
}
