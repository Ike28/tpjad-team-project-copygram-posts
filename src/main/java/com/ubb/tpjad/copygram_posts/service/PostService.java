package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidPostException;
import com.ubb.tpjad.copygram_posts.entity.Post;
import com.ubb.tpjad.copygram_posts.mapper.CommentMapper;
import com.ubb.tpjad.copygram_posts.mapper.PostMapper;
import com.ubb.tpjad.copygram_posts.repository.CommentLikeRepository;
import com.ubb.tpjad.copygram_posts.repository.CommentRepository;
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
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    public PostDto createPost(String description, MultipartFile photo, String userId) {
        val photoUploadResponse = photoClient.uploadPostPhoto(photo, description, userId);
        if (photoUploadResponse.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Photo upload returned unexpected result");
        }
        val postEntity = Post.builder()
                .userId(userId)
                .pictureId(photoUploadResponse.getBody().id().toString())
                .description(description)
                .build();
        val persisted = postRepository.save(postEntity);
        return postMapper.map(persisted);
    }

    public PostDto getPostWithMetadata(String postId) {
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
        val postLikes = postLikeRepository.countByPost_Id(postId);
        val postComments = commentRepository.findByPost_Id(postId);

        val mappedComments = postComments.stream()
                .map(commentEntity -> {
                    val commentLikes = commentLikeRepository.countByComment_Id(commentEntity.getId());
                    return commentMapper.map(commentEntity, commentLikes);
                })
                .toList();

        return postMapper.map(post, postLikes, mappedComments);
    }

    public String getPhotoIdForPost(String postId) {
        val post = postRepository.findById(postId).orElseThrow(() -> new InvalidPostException(postId));
        return post.getPictureId();
    }

    public void deletePost(String postId) {
        postRepository.deleteById(postId);
    }
}
