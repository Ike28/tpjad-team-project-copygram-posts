package com.ubb.tpjad.copygram_posts.controller;

import com.ubb.tpjad.copygram_posts.api.CopygramPostAPI;
import com.ubb.tpjad.copygram_posts.api.UserPostsResponse;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {
    @GetMapping(CopygramPostAPI.POSTS_ENDPOINT)
    public ResponseEntity<UserPostsResponse> getCurrentUserPosts(Authentication authentication) {
        return null;
    }

    @GetMapping(CopygramPostAPI.USERS_POSTS_ENDPOINT)
    public ResponseEntity<UserPostsResponse> getPostsByUserId(@RequestParam(CopygramPostAPI.USER_ID_QUERY_PARAM) String userId) {
        return null;
    }

    @GetMapping(CopygramPostAPI.POST_PICTURE_ENDPOINT)
    public ResponseEntity<?> getPostPicture(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId) {
        return null;
    }

    @GetMapping(CopygramPostAPI.POST_METADATA_ENDPOINT)
    public ResponseEntity<PostMetadataDto> getPostMetadata(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId) {
        return null;
    }

    @GetMapping(CopygramPostAPI.POSTS_RANDOM_ENDPOINT)
    public ResponseEntity<List<PostDto>> getRandomPosts(@RequestParam(CopygramPostAPI.POSTS_NUMBER_QUERY_PARAM) int postsCount) {
        return null;
    }

    @PostMapping(CopygramPostAPI.POST_UPLOAD_ENDPOINT)
    public ResponseEntity<PostDto> uploadPost(Authentication authentication) {
        return null;
    }

    @DeleteMapping(CopygramPostAPI.POSTS_ENDPOINT)
    public ResponseEntity<Void> deletePost(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                           Authentication authentication) {
        return null;
    }

    @PostMapping(CopygramPostAPI.POST_LIKES_ENDPOINT)
    public ResponseEntity<Void> postLike(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                         Authentication authentication) {
        return null;
    }

    @DeleteMapping(CopygramPostAPI.POST_LIKES_ENDPOINT)
    public ResponseEntity<Void> postUnlike(@RequestParam(CopygramPostAPI.POST_ID_QUERY_PARAM) String postId,
                                           Authentication authentication) {
        return null;
    }
}
