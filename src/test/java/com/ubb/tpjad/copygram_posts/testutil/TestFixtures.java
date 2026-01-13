package com.ubb.tpjad.copygram_posts.testutil;

public final class TestFixtures {

    // User IDs
    public static final String TEST_USER_ID = "user-123";
    public static final String TEST_USER_ID_2 = "user-456";
    public static final String TEST_USER_ID_3 = "user-789";

    // Post IDs
    public static final String TEST_POST_ID = "post-123";
    public static final String TEST_POST_ID_2 = "post-456";
    public static final String TEST_POST_ID_3 = "post-789";

    // Comment IDs
    public static final String TEST_COMMENT_ID = "comment-123";
    public static final String TEST_COMMENT_ID_2 = "comment-456";
    public static final String TEST_COMMENT_ID_3 = "comment-789";

    // Picture IDs
    public static final String TEST_PICTURE_ID = "picture-001";

    // Text content
    public static final String TEST_DESCRIPTION = "Test post description";
    public static final String TEST_COMMENT_TEXT = "Test comment text";

    // Photo metadata
    public static final Long TEST_PHOTO_ID = 1001L;
    public static final String TEST_FILENAME = "test-photo.jpg";
    public static final String TEST_CONTENT_TYPE = "image/jpeg";
    public static final Long TEST_FILE_SIZE = 1024L;
    public static final Integer TEST_WIDTH = 800;
    public static final Integer TEST_HEIGHT = 600;

    // HTTP headers
    public static final String TEST_AUTH_HEADER = "Bearer test-jwt-token";

    // File content
    public static final byte[] TEST_FILE_CONTENT = "test image content".getBytes();

    private TestFixtures() {
    }
}
