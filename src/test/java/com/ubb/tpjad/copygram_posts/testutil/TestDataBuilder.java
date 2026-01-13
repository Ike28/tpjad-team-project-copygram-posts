package com.ubb.tpjad.copygram_posts.testutil;

import com.ubb.tpjad.copygram_posts.api.dto.PhotoMetadataDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.entity.Comment;
import com.ubb.tpjad.copygram_posts.entity.CommentLike;
import com.ubb.tpjad.copygram_posts.entity.Post;
import com.ubb.tpjad.copygram_posts.entity.PostLike;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.ubb.tpjad.copygram_posts.testutil.TestFixtures.*;

public final class TestDataBuilder {

    // ==================== Entity Builders ====================

    public static Post buildPost() {
        return buildPost(TEST_POST_ID, TEST_USER_ID, TEST_PICTURE_ID, TEST_DESCRIPTION);
    }

    public static Post buildPost(String id, String userId) {
        return buildPost(id, userId, TEST_PICTURE_ID, TEST_DESCRIPTION);
    }

    public static Post buildPost(String id, String userId, String pictureId, String description) {
        return Post.builder()
                .id(id)
                .userId(userId)
                .pictureId(pictureId)
                .description(description)
                .createdAt(Instant.now())
                .build();
    }

    public static Comment buildComment() {
        return buildComment(TEST_COMMENT_ID, TEST_USER_ID, buildPost(), TEST_COMMENT_TEXT);
    }

    public static Comment buildComment(String id, String userId, Post post) {
        return buildComment(id, userId, post, TEST_COMMENT_TEXT);
    }

    public static Comment buildComment(String id, String userId, Post post, String text) {
        return Comment.builder()
                .id(id)
                .userId(userId)
                .post(post)
                .text(text)
                .createdAt(Instant.now())
                .build();
    }

    public static PostLike buildPostLike(String userId, Post post) {
        return PostLike.builder()
                .id("like-" + userId + "-" + post.getId())
                .userId(userId)
                .post(post)
                .createdAt(Instant.now())
                .build();
    }

    public static CommentLike buildCommentLike(String userId, Comment comment) {
        return CommentLike.builder()
                .id("like-" + userId + "-" + comment.getId())
                .userId(userId)
                .comment(comment)
                .createdAt(Instant.now())
                .build();
    }

    // ==================== DTO Builders ====================

    public static PostDto buildPostDto() {
        return buildPostDto(TEST_POST_ID, TEST_USER_ID, TEST_PICTURE_ID, TEST_DESCRIPTION);
    }

    public static PostDto buildPostDto(String id, String userId) {
        return buildPostDto(id, userId, TEST_PICTURE_ID, TEST_DESCRIPTION);
    }

    public static PostDto buildPostDto(String id, String userId, String pictureId, String description) {
        return PostDto.builder()
                .id(id)
                .userId(userId)
                .pictureId(pictureId)
                .description(description)
                .build();
    }

    public static PostCommentDto buildPostCommentDto() {
        return buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID, TEST_COMMENT_TEXT, 0L);
    }

    public static PostCommentDto buildPostCommentDto(String id, String postId, String userId) {
        return buildPostCommentDto(id, postId, userId, TEST_COMMENT_TEXT, 0L);
    }

    public static PostCommentDto buildPostCommentDto(String id, String postId, String userId, String text, long likesCount) {
        return PostCommentDto.builder()
                .id(id)
                .postId(postId)
                .userId(userId)
                .text(text)
                .likesCount(likesCount)
                .build();
    }

    public static PhotoMetadataDto buildPhotoMetadataDto() {
        return buildPhotoMetadataDto(TEST_PHOTO_ID);
    }

    public static PhotoMetadataDto buildPhotoMetadataDto(Long id) {
        return new PhotoMetadataDto(
                id,
                TEST_FILENAME,
                TEST_CONTENT_TYPE,
                TEST_FILE_SIZE,
                TEST_WIDTH,
                TEST_HEIGHT,
                LocalDateTime.now()
        );
    }

    // ==================== MultipartFile Builders ====================

    public static MultipartFile buildMultipartFile() {
        return buildMultipartFile(TEST_FILENAME, TEST_FILE_CONTENT);
    }

    public static MultipartFile buildMultipartFile(String filename) {
        return buildMultipartFile(filename, TEST_FILE_CONTENT);
    }

    public static MultipartFile buildMultipartFile(String filename, byte[] content) {
        return new MockMultipartFile(
                "file",
                filename,
                TEST_CONTENT_TYPE,
                content
        );
    }

    public static MultipartFile buildMultipartFileWithIOException() {
        return new MockMultipartFile("file", TEST_FILENAME, TEST_CONTENT_TYPE, TEST_FILE_CONTENT) {
            @Override
            public byte[] getBytes() {
                throw new RuntimeException("Simulated IO exception");
            }
        };
    }

    private TestDataBuilder() {
    }
}
