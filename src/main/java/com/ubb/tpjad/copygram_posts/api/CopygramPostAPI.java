package com.ubb.tpjad.copygram_posts.api;

public final class CopygramPostAPI {
    private CopygramPostAPI() {}

    // Paths
    private static final String POSTS_PATH = "posts";
    private static final String USERS_PATH = "users";
    private static final String PICTURES_PATH = "pictures";
    private static final String COMMENTS_PATH = "comments";
    private static final String METADATA_PATH = "metadata";
    private static final String LIKES_PATH = "likes";
    private static final String UPLOAD_PATH = "upload";
    private static final String RANDOM_PATH = "random";

    // Endpoints
    public static final String POSTS_ENDPOINT = "/" + POSTS_PATH;
    public static final String USERS_POSTS_ENDPOINT = POSTS_ENDPOINT + "/" + USERS_PATH;
    public static final String POSTS_RANDOM_ENDPOINT = POSTS_ENDPOINT + "/" + RANDOM_PATH;
    public static final String POST_COMMENTS_ENDPOINT = POSTS_ENDPOINT + "/" + COMMENTS_PATH;
    public static final String COMMENT_LIKES_ENDPOINT = POST_COMMENTS_ENDPOINT + "/" + LIKES_PATH;
    public static final String POST_PICTURE_ENDPOINT = POSTS_ENDPOINT + "/" + PICTURES_PATH;
    public static final String POST_METADATA_ENDPOINT = POSTS_ENDPOINT + "/" + METADATA_PATH;
    public static final String POST_UPLOAD_ENDPOINT = POSTS_ENDPOINT + "/" + UPLOAD_PATH;
    public static final String POST_LIKES_ENDPOINT = POSTS_ENDPOINT + "/" + LIKES_PATH;

    // Query params
    public static final String USER_ID_QUERY_PARAM = "user_id";
    public static final String POST_ID_QUERY_PARAM = "post_id";
    public static final String COMMENT_ID_QUERY_PARAM = "comment_id";
    public static final String POSTS_NUMBER_QUERY_PARAM = "posts_count";
}
