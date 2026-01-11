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
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Photo upload returned unexpected result");
        }
        val postEntity = mapper.map(userId, photoUploadResponse.getBody().id().toString(), description);
        val persisted = postRepository.save(postEntity);
        return mapper.map(persisted);
    }

    public PostMetadataDto getPostMetadata(String postId) {
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
        val postLikes = postLikeRepository.countByPost_Id(postId);

        val mappedComments = commentService.getPostCommentsWithMetadata(postId);

        return mapper.map(post, postLikes, mappedComments);
    }

    public UserPostsResponse getPostsByUserId(String userId) {
        val userPosts = postRepository.findByUserId(userId)
                .stream()
                .map(mapper::map)
                .toList();
        return new UserPostsResponse(userId, userPosts);
    }

    public String getPhotoIdForPost(String postId) {
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
        return post.getPictureId();
    }

    public void deletePost(String postId, String userId) {
        if (!postRepository.existsByIdAndUserId(postId, userId)) {
            throw UnauthorizedDeletionException.unauthorizedPostDelete(userId, postId);
        }
        postRepository.deleteById(postId);
    }

    public void postLike(String postId, String userId) {
        if (postLikeRepository.existsByUserIdAndPost_Id(userId, postId)) {
            throw InvalidLikeActionException.duplicatePostLike(postId, userId);
        }

        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
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
        postLikeRepository.deleteByUserIdAndPost_Id(userId, postId);
    }
}
