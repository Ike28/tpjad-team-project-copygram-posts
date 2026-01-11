package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.UserPostsResponse;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import com.ubb.tpjad.copygram_posts.api.exception.LimitExceededException;
import com.ubb.tpjad.copygram_posts.service.PostService;
import com.ubb.tpjad.copygram_posts.service.RemotePhotoClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Posts", description = "Endpoints for posts creation, deletion, retrieval and liking/unliking. Comment retrieval is also included here.")
public class PostController {
    private final PostService postService;
    private final RemotePhotoClient photoClient;

    @Value("${posts.request-limit:150}")
    private int postRequestLimit;

    @Operation(summary = "Get posts for current logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user's posts",
                    content = @Content(schema = @Schema(implementation = UserPostsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @GetMapping(CopygramPostAPI.POSTS_ENDPOINT)
    public ResponseEntity<UserPostsResponse> getCurrentUserPosts(Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received current user posts request from user {}", userId);

        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @Operation(summary = "Get posts for specified user by userId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of user's posts",
                    content = @Content(schema = @Schema(implementation = UserPostsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @GetMapping(CopygramPostAPI.USERS_POSTS_ENDPOINT)
    public ResponseEntity<UserPostsResponse> getPostsByUserId(@RequestParam(CopygramPostAPI.USER_ID_QUERY_PARAM) String userId) {
        log.info("Received user posts request for user {}", userId);

        return ResponseEntity.ok(postService.getPostsByUserId(userId));
    }

    @Operation(summary = "Get post photo by postId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo retrieved from server",
                    content = @Content(schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "400", description = "Invalid postId provided",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid postId 1\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @GetMapping(CopygramPostAPI.POST_PICTURE_ENDPOINT)
    public ResponseEntity<byte[]> getPostPicture(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                            HttpServletRequest request) {
        log.info("Received post photo retrieval request for postId {}", postId);
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

    @Operation(summary = "Get post metadata by postId")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post metadata",
                    content = @Content(schema = @Schema(implementation = PostMetadataDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid postId provided",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid postId 1\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @GetMapping(CopygramPostAPI.POST_METADATA_ENDPOINT)
    public ResponseEntity<PostMetadataDto> getPostMetadata(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId) {
        log.info("Received post metadata request for postId {}", postId);

        val postMetadata = postService.getPostMetadata(postId);
        return ResponseEntity.ok(postMetadata);
    }

    @Operation(summary = "Get randomly selected posts, up to specified limit if available")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of randomly selected posts",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "204", description = "No posts are available or negative specified limit"),
            @ApiResponse(responseCode = "401", description = "Request was not authorized"),
            @ApiResponse(responseCode = "413", description = "Limit specified is too high, check provided limit",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Limit specified is too high, check provided limit\", \"limit\": 100}")))
    })
    @GetMapping(CopygramPostAPI.POSTS_RANDOM_ENDPOINT)
    public ResponseEntity<List<PostDto>> getRandomPosts(@RequestParam(CopygramPostAPI.POSTS_NUMBER_QUERY_PARAM) int postsCount) {
        log.info("Received random posts retrieval request with limit {}", postsCount);
        if (postsCount < 0) {
            return ResponseEntity.noContent().build();
        }
        if (postsCount > postRequestLimit) {
            throw new LimitExceededException(postsCount, postRequestLimit);
        }

        val randomPosts = postService.getRandomPosts(postsCount);
        return randomPosts.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(randomPosts);
    }

    @Operation(summary = "Upload post with image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created",
                    content = @Content(schema = @Schema(implementation = PostDto.class))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized"),
            @ApiResponse(responseCode = "502", description = "Photo upload service returned unexpected result")
    })
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
        log.info("Received new post upload request from user {}", userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(description, photoFile, userId));
    }

    @Operation(summary = "Save post like by postId, on behalf of logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post like saved"),
            @ApiResponse(responseCode = "400", description = "Invalid postId provided",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid postId 1\"}"))),
            @ApiResponse(responseCode = "400", description = "Duplicate like request attempted",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Duplicate like request for entity 1 of type POST from user abc\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @PostMapping(CopygramPostAPI.POST_LIKES_ENDPOINT)
    public ResponseEntity<Void> postLike(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                         Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received post like request from user {} for post {}", userId, postId);

        postService.postLike(postId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete post like by postId, on behalf of logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post like deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid postId provided",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid postId 1\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid unlike request attempted",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid unlike request for entity 1 of type POST from user abc\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @DeleteMapping(CopygramPostAPI.POST_LIKES_ENDPOINT)
    public ResponseEntity<Void> postUnlike(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                           Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received post unlike request from user {} for post {}", userId, postId);

        postService.postUnlike(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete logged-in user's post by postId, will also delete likes, comments and comment likes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "401", description = "Request was not authorized"),
            @ApiResponse(responseCode = "403", description = "User is forbidden to delete post, most likely attempting to delete other user's post",
                    content = @Content(schema = @Schema(example = "{\"error\": \"User abc is unauthorized to delete entity 1 of type POST\"}")))
    })
    @DeleteMapping(CopygramPostAPI.POSTS_ENDPOINT)
    public ResponseEntity<Void> deletePost(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                           Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received post delete request from user {} for post {}", userId, postId);

        postService.deletePost(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
