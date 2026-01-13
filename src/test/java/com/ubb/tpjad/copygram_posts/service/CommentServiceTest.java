package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidCommentException;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidLikeActionException;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidPostException;
import com.ubb.tpjad.copygram_posts.api.exception.UnauthorizedDeletionException;
import com.ubb.tpjad.copygram_posts.entity.Comment;
import com.ubb.tpjad.copygram_posts.entity.CommentLike;
import com.ubb.tpjad.copygram_posts.entity.Post;
import com.ubb.tpjad.copygram_posts.mapper.CommentMapper;
import com.ubb.tpjad.copygram_posts.repository.CommentLikeRepository;
import com.ubb.tpjad.copygram_posts.repository.CommentRepository;
import com.ubb.tpjad.copygram_posts.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ubb.tpjad.copygram_posts.testutil.TestDataBuilder.*;
import static com.ubb.tpjad.copygram_posts.testutil.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentMapper mapper;

    @InjectMocks
    private CommentService commentService;

    // ==================== postComment() Tests ====================

    @Test
    void postComment_ValidPayload_CreatesAndReturnsComment() {
        // Given
        PostCommentDto payload = buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID);
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        Comment comment = buildComment(TEST_COMMENT_ID, TEST_USER_ID, post);
        PostCommentDto expectedDto = buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID, TEST_COMMENT_TEXT, 0L);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(post));
        when(mapper.map(payload, post)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(mapper.map(comment, 0L)).thenReturn(expectedDto);

        // When
        PostCommentDto result = commentService.postComment(payload);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(TEST_COMMENT_ID);
        assertThat(result.postId()).isEqualTo(TEST_POST_ID);
        assertThat(result.userId()).isEqualTo(TEST_USER_ID);
        assertThat(result.likesCount()).isZero();
        verify(postRepository).findById(TEST_POST_ID);
        verify(commentRepository).save(comment);
    }

    @Test
    void postComment_InvalidPostId_ThrowsInvalidPostException() {
        // Given
        PostCommentDto payload = buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID);
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        // When & Then
        InvalidPostException exception = assertThrows(
                InvalidPostException.class,
                () -> commentService.postComment(payload)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void postComment_PayloadWithId_UsesProvidedId() {
        // Given
        String explicitId = "explicit-comment-id";
        PostCommentDto payload = buildPostCommentDto(explicitId, TEST_POST_ID, TEST_USER_ID);
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        Comment comment = buildComment(explicitId, TEST_USER_ID, post);
        PostCommentDto expectedDto = buildPostCommentDto(explicitId, TEST_POST_ID, TEST_USER_ID, TEST_COMMENT_TEXT, 0L);

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(post));
        when(mapper.map(payload, post)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(mapper.map(comment, 0L)).thenReturn(expectedDto);

        // When
        PostCommentDto result = commentService.postComment(payload);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(explicitId);
        verify(commentRepository).save(comment);
    }

    // ==================== getPostCommentsWithMetadata() Tests ====================

    @Test
    void getPostCommentsWithMetadata_PostHasComments_ReturnsCommentsWithLikeCounts() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        Comment comment1 = buildComment(TEST_COMMENT_ID, TEST_USER_ID, post);
        Comment comment2 = buildComment(TEST_COMMENT_ID_2, TEST_USER_ID_2, post);
        Comment comment3 = buildComment(TEST_COMMENT_ID_3, TEST_USER_ID_3, post);
        List<Comment> comments = List.of(comment1, comment2, comment3);

        PostCommentDto dto1 = buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID, TEST_COMMENT_TEXT, 5L);
        PostCommentDto dto2 = buildPostCommentDto(TEST_COMMENT_ID_2, TEST_POST_ID, TEST_USER_ID_2, TEST_COMMENT_TEXT, 2L);
        PostCommentDto dto3 = buildPostCommentDto(TEST_COMMENT_ID_3, TEST_POST_ID, TEST_USER_ID_3, TEST_COMMENT_TEXT, 0L);

        when(commentRepository.findByPost_Id(TEST_POST_ID)).thenReturn(comments);
        when(commentLikeRepository.countByComment_Id(TEST_COMMENT_ID)).thenReturn(5L);
        when(commentLikeRepository.countByComment_Id(TEST_COMMENT_ID_2)).thenReturn(2L);
        when(commentLikeRepository.countByComment_Id(TEST_COMMENT_ID_3)).thenReturn(0L);
        when(mapper.map(comment1, 5L)).thenReturn(dto1);
        when(mapper.map(comment2, 2L)).thenReturn(dto2);
        when(mapper.map(comment3, 0L)).thenReturn(dto3);

        // When
        List<PostCommentDto> result = commentService.getPostCommentsWithMetadata(TEST_POST_ID);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).likesCount()).isEqualTo(5L);
        assertThat(result.get(1).likesCount()).isEqualTo(2L);
        assertThat(result.get(2).likesCount()).isZero();
        verify(commentLikeRepository, times(3)).countByComment_Id(any());
    }

    @Test
    void getPostCommentsWithMetadata_PostHasNoComments_ReturnsEmptyList() {
        // Given
        when(commentRepository.findByPost_Id(TEST_POST_ID)).thenReturn(Collections.emptyList());

        // When
        List<PostCommentDto> result = commentService.getPostCommentsWithMetadata(TEST_POST_ID);

        // Then
        assertThat(result).isEmpty();
        verify(commentLikeRepository, never()).countByComment_Id(any());
    }

    @Test
    void getPostCommentsWithMetadata_CommentsWithNoLikes_ReturnsZeroLikeCounts() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        Comment comment1 = buildComment(TEST_COMMENT_ID, TEST_USER_ID, post);
        Comment comment2 = buildComment(TEST_COMMENT_ID_2, TEST_USER_ID_2, post);
        List<Comment> comments = List.of(comment1, comment2);

        PostCommentDto dto1 = buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID, TEST_COMMENT_TEXT, 0L);
        PostCommentDto dto2 = buildPostCommentDto(TEST_COMMENT_ID_2, TEST_POST_ID, TEST_USER_ID_2, TEST_COMMENT_TEXT, 0L);

        when(commentRepository.findByPost_Id(TEST_POST_ID)).thenReturn(comments);
        when(commentLikeRepository.countByComment_Id(TEST_COMMENT_ID)).thenReturn(0L);
        when(commentLikeRepository.countByComment_Id(TEST_COMMENT_ID_2)).thenReturn(0L);
        when(mapper.map(comment1, 0L)).thenReturn(dto1);
        when(mapper.map(comment2, 0L)).thenReturn(dto2);

        // When
        List<PostCommentDto> result = commentService.getPostCommentsWithMetadata(TEST_POST_ID);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).likesCount()).isZero();
        assertThat(result.get(1).likesCount()).isZero();
    }

    // ==================== commentLike() Tests ====================

    @Test
    void commentLike_ValidRequest_CreatesLike() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        Comment comment = buildComment(TEST_COMMENT_ID, TEST_USER_ID, post);

        when(commentLikeRepository.existsByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(false);
        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.of(comment));

        // When
        assertDoesNotThrow(() -> commentService.commentLike(TEST_COMMENT_ID, TEST_USER_ID));

        // Then
        ArgumentCaptor<CommentLike> captor = ArgumentCaptor.forClass(CommentLike.class);
        verify(commentLikeRepository).save(captor.capture());

        CommentLike savedLike = captor.getValue();
        assertThat(savedLike.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedLike.getComment()).isEqualTo(comment);
    }

    @Test
    void commentLike_DuplicateLike_ThrowsInvalidLikeActionException() {
        // Given
        when(commentLikeRepository.existsByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(true);

        // When & Then
        InvalidLikeActionException exception = assertThrows(
                InvalidLikeActionException.class,
                () -> commentService.commentLike(TEST_COMMENT_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_COMMENT_ID);
        assertThat(exception.getMessage()).contains(TEST_USER_ID);
        verify(commentLikeRepository, never()).save(any());
    }

    @Test
    void commentLike_InvalidCommentId_ThrowsInvalidCommentException() {
        // Given
        when(commentLikeRepository.existsByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(false);
        when(commentRepository.findById(TEST_COMMENT_ID)).thenReturn(Optional.empty());

        // When & Then
        InvalidCommentException exception = assertThrows(
                InvalidCommentException.class,
                () -> commentService.commentLike(TEST_COMMENT_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_COMMENT_ID);
        verify(commentLikeRepository, never()).save(any());
    }

    // ==================== commentUnlike() Tests ====================

    @Test
    void commentUnlike_ValidRequest_RemovesLike() {
        // Given
        when(commentLikeRepository.existsByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> commentService.commentUnlike(TEST_COMMENT_ID, TEST_USER_ID));

        // Then
        verify(commentLikeRepository).existsByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID);
        verify(commentLikeRepository).deleteByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID);
    }

    @Test
    void commentUnlike_NoExistingLike_ThrowsInvalidLikeActionException() {
        // Given
        when(commentLikeRepository.existsByUserIdAndComment_Id(TEST_USER_ID, TEST_COMMENT_ID)).thenReturn(false);

        // When & Then
        InvalidLikeActionException exception = assertThrows(
                InvalidLikeActionException.class,
                () -> commentService.commentUnlike(TEST_COMMENT_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_COMMENT_ID);
        assertThat(exception.getMessage()).contains(TEST_USER_ID);
        verify(commentLikeRepository, never()).deleteByUserIdAndComment_Id(any(), any());
    }

    // ==================== deleteComment() Tests ====================

    @Test
    void deleteComment_AuthorizedUser_DeletesComment() {
        // Given
        when(commentRepository.existsByIdAndUserId(TEST_COMMENT_ID, TEST_USER_ID)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> commentService.deleteComment(TEST_COMMENT_ID, TEST_USER_ID));

        // Then
        verify(commentRepository).existsByIdAndUserId(TEST_COMMENT_ID, TEST_USER_ID);
        verify(commentRepository).deleteById(TEST_COMMENT_ID);
    }

    @Test
    void deleteComment_UnauthorizedUser_ThrowsUnauthorizedDeletionException() {
        // Given
        when(commentRepository.existsByIdAndUserId(TEST_COMMENT_ID, TEST_USER_ID_2)).thenReturn(false);

        // When & Then
        UnauthorizedDeletionException exception = assertThrows(
                UnauthorizedDeletionException.class,
                () -> commentService.deleteComment(TEST_COMMENT_ID, TEST_USER_ID_2)
        );

        assertThat(exception.getMessage()).contains(TEST_USER_ID_2);
        assertThat(exception.getMessage()).contains(TEST_COMMENT_ID);
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void deleteComment_NonExistentComment_ThrowsUnauthorizedDeletionException() {
        // Given
        when(commentRepository.existsByIdAndUserId(TEST_COMMENT_ID, TEST_USER_ID)).thenReturn(false);

        // When & Then
        UnauthorizedDeletionException exception = assertThrows(
                UnauthorizedDeletionException.class,
                () -> commentService.deleteComment(TEST_COMMENT_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_COMMENT_ID);
        verify(commentRepository, never()).deleteById(any());
    }
}
