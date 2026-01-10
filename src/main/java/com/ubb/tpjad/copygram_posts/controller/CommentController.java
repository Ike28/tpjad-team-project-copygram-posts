package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class CommentController {
    @PostMapping(CopygramPostAPI.POST_COMMENTS_ENDPOINT)
    public ResponseEntity<PostCommentDto> postComment(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                                      Authentication authentication) {
        return null;
    }

    @PostMapping(CopygramPostAPI.COMMENT_LIKES_ENDPOINT)
    public ResponseEntity<Void> commentLike(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                            Authentication authentication) {
        return null;
    }

    @DeleteMapping(CopygramPostAPI.COMMENT_LIKES_ENDPOINT)
    public ResponseEntity<Void> commentUnlike(@RequestParam(CopygramPostAPI.COMMENT_ID_QUERY_PARAM) String commentId,
                                              Authentication authentication) {
        return null;
    }
}
