package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Comments", description = "Endpoints for comments creation, deletion and liking/unliking.")
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "Upload comment on post, only post_id and text are required")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post comment created",
                    content = @Content(schema = @Schema(implementation = PostCommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid postId provided in payload",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid postId 1\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @PostMapping(CopygramPostAPI.POST_COMMENTS_ENDPOINT)
    public ResponseEntity<PostCommentDto> postComment(@RequestBody PostCommentDto commentDto,
                                                      Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received new post comment request from user {}", userId);

        val comment = commentService.postComment(PostCommentDto.builder()
                        .postId(commentDto.postId())
                        .text(commentDto.text())
                        .userId(userId)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comment);
    }

    @Operation(summary = "Save comment like by commentId, on behalf of logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment like saved"),
            @ApiResponse(responseCode = "400", description = "Invalid commentId provided",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid commentId 1\"}"))),
            @ApiResponse(responseCode = "400", description = "Duplicate like request attempted",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Duplicate like request for entity 1 of type COMMENT from user abc\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @PostMapping(CopygramPostAPI.COMMENT_LIKES_ENDPOINT)
    public ResponseEntity<Void> commentLike(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                            Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received new comment like request from user {} for comment {}", userId, commentId);

        commentService.commentLike(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete comment like by commentId, on behalf of logged-in user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment like deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid commentId provided",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid commentId 1\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid unlike request attempted",
                    content = @Content(schema = @Schema(example = "{\"error\": \"Invalid unlike request for entity 1 of type COMMENT from user abc\"}"))),
            @ApiResponse(responseCode = "401", description = "Request was not authorized")
    })
    @DeleteMapping(CopygramPostAPI.COMMENT_LIKES_ENDPOINT)
    public ResponseEntity<Void> commentUnlike(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                              Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received new comment unlike request from user {} for comment {}", userId, commentId);

        commentService.commentUnlike(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete logged-in user's comment by commentId, will also delete likes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted"),
            @ApiResponse(responseCode = "401", description = "Request was not authorized"),
            @ApiResponse(responseCode = "403", description = "User is forbidden to delete comment, most likely attempting to delete other user's comment",
                    content = @Content(schema = @Schema(example = "{\"error\": \"User abc is unauthorized to delete entity 1 of type COMMENT\"}")))
    })
    @DeleteMapping(CopygramPostAPI.POST_COMMENTS_ENDPOINT)
    public ResponseEntity<PostCommentDto> deleteComment(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                                        Authentication authentication) {
        val userId = authentication.getName();
        log.info("Received new delete comment request from user {} for comment {}", userId, commentId);
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
