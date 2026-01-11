package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.service.CommentService;
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
public class CommentController {
    private final CommentService commentService;
    
    @PostMapping(CopygramPostAPI.POST_COMMENTS_ENDPOINT)
    public ResponseEntity<PostCommentDto> postComment(@RequestBody PostCommentDto commentDto,
                                                      Authentication authentication) {
        val userId = authentication.getName();
        val comment = commentService.postComment(PostCommentDto.builder()
                        .postId(commentDto.postId())
                        .text(commentDto.text())
                        .userId(userId)
                .build());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comment);
    }

    @PostMapping(CopygramPostAPI.COMMENT_LIKES_ENDPOINT)
    public ResponseEntity<Void> commentLike(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                            Authentication authentication) {
        val userId = authentication.getName();
        commentService.commentLike(commentId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(CopygramPostAPI.COMMENT_LIKES_ENDPOINT)
    public ResponseEntity<Void> commentUnlike(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                              Authentication authentication) {
        val userId = authentication.getName();
        commentService.commentUnlike(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(CopygramPostAPI.POST_COMMENTS_ENDPOINT)
    public ResponseEntity<PostCommentDto> deleteComment(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                                        Authentication authentication) {
        val userId = authentication.getName();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
