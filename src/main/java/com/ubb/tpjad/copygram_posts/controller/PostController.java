package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.UserPostsResponse;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import com.ubb.tpjad.copygram_posts.service.PostService;
import com.ubb.tpjad.copygram_posts.service.RemotePhotoClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {
    private final PostService postService;
    private final RemotePhotoClient photoClient;

    @Value("${posts.request-limit:150}")
    private int postRequestLimit;

    @GetMapping(CopygramPostAPI.POSTS_ENDPOINT)
    public ResponseEntity<UserPostsResponse> getCurrentUserPosts(Authentication authentication) {
        val userId = authentication.getName();
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @GetMapping(CopygramPostAPI.USERS_POSTS_ENDPOINT)
    public ResponseEntity<UserPostsResponse> getPostsByUserId(@RequestParam(CopygramPostAPI.USER_ID_QUERY_PARAM) String userId) {
        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @GetMapping(CopygramPostAPI.POST_PICTURE_ENDPOINT)
    public ResponseEntity<byte[]> getPostPicture(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                            HttpServletRequest request) {
        val photoId = postService.getPhotoIdForPost(postId);
        val remoteResponse = photoClient.retrievePostPhoto(photoId, request.getHeader(HttpHeaders.AUTHORIZATION));

        if (remoteResponse.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found");
        }

        val contentType = Optional.ofNullable(remoteResponse.getHeaders().getContentType())
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.status(remoteResponse.getStatusCode())
                .contentType(contentType)
                .contentLength(remoteResponse.getBody().length)
                .body(remoteResponse.getBody());
    }

    @GetMapping(CopygramPostAPI.POST_METADATA_ENDPOINT)
    public ResponseEntity<PostMetadataDto> getPostMetadata(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId) {
        val postMetadata = postService.getPostMetadata(postId);
        return ResponseEntity.ok(postMetadata);
    }

    @GetMapping(CopygramPostAPI.POSTS_RANDOM_ENDPOINT)
    public ResponseEntity<List<PostDto>> getRandomPosts(@RequestParam(CopygramPostAPI.POSTS_NUMBER_QUERY_PARAM) int postsCount) {
        if (postsCount < 0) {
            return ResponseEntity.noContent().build();
        }
        if (postsCount > postRequestLimit) {
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).build();
        }

        val randomPosts = postService.getRandomPosts(postsCount);
        return ResponseEntity.ok(randomPosts);
    }

    @PostMapping(
            value = CopygramPostAPI.POST_UPLOAD_ENDPOINT,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PostDto> uploadPost(
            @RequestParam(CopygramPostAPI.POST_DESCRIPTION_REQUEST_PARAM) String description,
            @RequestParam(CopygramPostAPI.POST_PHOTO_FILE_REQUEST_PARAM) MultipartFile photoFile,
            Authentication authentication) {
        val userId = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(description, photoFile, userId));
    }

    @DeleteMapping(CopygramPostAPI.POSTS_ENDPOINT)
    public ResponseEntity<Void> deletePost(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                           Authentication authentication) {
        val userId = authentication.getName();
        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(CopygramPostAPI.POST_LIKES_ENDPOINT)
    public ResponseEntity<Void> postLike(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                         Authentication authentication) {
        val userId = authentication.getName();
        postService.postLike(postId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(CopygramPostAPI.POST_LIKES_ENDPOINT)
    public ResponseEntity<Void> postUnlike(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                           Authentication authentication) {
        val userId = authentication.getName();
        postService.postUnlike(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
