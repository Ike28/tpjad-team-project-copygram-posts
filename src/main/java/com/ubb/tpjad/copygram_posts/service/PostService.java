package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.UserPostsResponse;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidLikeActionException;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidPostException;
import com.ubb.tpjad.copygram_posts.api.exception.UnauthorizedDeletionException;
import com.ubb.tpjad.copygram_posts.entity.PostLike;
import com.ubb.tpjad.copygram_posts.mapper.PostMapper;
import com.ubb.tpjad.copygram_posts.repository.PostLikeRepository;
import com.ubb.tpjad.copygram_posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final RemotePhotoClient photoClient;
    private final CommentService commentService;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMapper mapper;

    public PostDto createPost(String description, MultipartFile photo, String userId) {
        val photoUploadResponse = photoClient.uploadPostPhoto(photo, description, userId);
        if (photoUploadResponse.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Photo upload service returned unexpected result");
        }
        val postEntity = mapper.map(userId, photoUploadResponse.getBody().id().toString(), description);
        val persisted = postRepository.save(postEntity);
        return mapper.map(persisted);
    }

    public PostMetadataDto getPostMetadata(String postId) {
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
        val postLikes = postLikeRepository.countByPost_Id(postId);
        log.info("Retrieved {} likes for post {}", postLikes, postId);

        val mappedComments = commentService.getPostCommentsWithMetadata(postId);
        log.info("Retrieved {} comments for post {}", mappedComments, postId);

        return mapper.map(post, postLikes, mappedComments);
    }

    public UserPostsResponse getPostsByUserId(String userId) {
        val userPosts = postRepository.findByUserId(userId)
                .stream()
                .map(mapper::map)
                .toList();
        log.info("Retrieved {} posts for user {}", userPosts.size(), userId);
        return new UserPostsResponse(userId, userPosts);
    }

    public List<PostDto> getRandomPosts(int limit) {
        val selected = postRepository.findRandomPosts(limit)
                .stream()
                .map(mapper::map)
                .toList();

        log.info("Retrieved {} random posts", selected.size());
        return selected;
    }

    public String getPhotoIdForPost(String postId) {
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
        log.info("Linked post {} with picture {}", postId, post.getPictureId());

        return post.getPictureId();
    }

    public void deletePost(String postId, String userId) {
        if (!postRepository.existsByIdAndUserId(postId, userId)) {
            throw UnauthorizedDeletionException.unauthorizedPostDelete(userId, postId);
        }
        log.info("Post {} identified from user {}, proceeding with deletion.", postId, userId);
        postRepository.deleteById(postId);
    }

    public void postLike(String postId, String userId) {
        if (postLikeRepository.existsByUserIdAndPost_Id(userId, postId)) {
            throw InvalidLikeActionException.duplicatePostLike(postId, userId);
        }
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));

        log.info("Validation of post like request succeeded, saving like for post {} from user {}", postId, userId);
        val postLikeEntity = PostLike.builder()
                .userId(userId)
                .post(post)
                .build();
        postLikeRepository.save(postLikeEntity);
    }

    public void postUnlike(String postId, String userId) {
        if (!postLikeRepository.existsByUserIdAndPost_Id(userId, postId)) {
            throw InvalidLikeActionException.invalidPostUnlike(postId, userId);
        }
        log.info("Validation of post unlike request succeeded, removing like from post {} for user {}", postId, userId);
        postLikeRepository.deleteByUserIdAndPost_Id(userId, postId);
    }
}
