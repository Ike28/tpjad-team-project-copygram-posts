package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidCommentException;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidLikeActionException;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidPostException;
import com.ubb.tpjad.copygram_posts.api.exception.UnauthorizedDeletionException;
import com.ubb.tpjad.copygram_posts.entity.CommentLike;
import com.ubb.tpjad.copygram_posts.mapper.CommentMapper;
import com.ubb.tpjad.copygram_posts.repository.CommentLikeRepository;
import com.ubb.tpjad.copygram_posts.repository.CommentRepository;
import com.ubb.tpjad.copygram_posts.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentMapper mapper;

    public PostCommentDto postComment(PostCommentDto payload) {
        val post = postRepository.findById(payload.postId()).orElseThrow(() -> new InvalidPostException(payload.postId()));
        val mappedEntity = mapper.map(payload, post);
        log.info("Mapped comment payload to entity with id {}", mappedEntity.getId());

        val commentEntity = commentRepository.save(mappedEntity);
        log.info("Successfully persisted comment with id {}", commentEntity.getId());
        return mapper.map(commentEntity, 0);
    }

    public List<PostCommentDto> getPostCommentsWithMetadata(String postId) {
        val postComments = commentRepository.findByPost_Id(postId);

        return postComments.stream()
                .map(commentEntity -> {
                    val commentLikes = commentLikeRepository.countByComment_Id(commentEntity.getId());
                    return mapper.map(commentEntity, commentLikes);
                })
                .toList();
    }

    public void commentLike(String commentId, String userId) {
        if (commentLikeRepository.existsByUserIdAndComment_Id(userId, commentId)) {
            throw InvalidLikeActionException.duplicateCommentLike(commentId, userId);
        }
        log.info("Validation of comment like request succeeded, saving like for comment {} from user {}", commentId, userId);

        val comment = commentRepository.findById(commentId).orElseThrow(() -> new InvalidCommentException(commentId));
        val commentLikeEntity = CommentLike.builder()
                .userId(userId)
                .comment(comment)
                .build();
        commentLikeRepository.save(commentLikeEntity);
    }

    public void commentUnlike(String commentId, String userId) {
        if (!commentLikeRepository.existsByUserIdAndComment_Id(userId, commentId)) {
            throw InvalidLikeActionException.invalidCommentUnlike(commentId, userId);
        }
        log.info("Validation of comment unlike request succeeded, removing like from comment {} for user {}", commentId, userId);

        commentLikeRepository.deleteByUserIdAndComment_Id(userId, commentId);
    }

    public void deleteComment(String commentId, String userId) {
        if (!commentRepository.existsByIdAndUserId(commentId, userId)) {
            throw UnauthorizedDeletionException.unauthorizedCommentDelete(userId, commentId);
        }
        log.info("Comment {} identified from user {}, proceeding with deletion.", commentId, userId);

        commentRepository.deleteById(commentId);
    }
}
