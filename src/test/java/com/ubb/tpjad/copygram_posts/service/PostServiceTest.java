package com.ubb.tpjad.copygram_posts.service;

import com.ubb.tpjad.copygram_posts.api.UserPostsResponse;
import com.ubb.tpjad.copygram_posts.api.dto.PhotoMetadataDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidLikeActionException;
import com.ubb.tpjad.copygram_posts.api.exception.InvalidPostException;
import com.ubb.tpjad.copygram_posts.api.exception.UnauthorizedDeletionException;
import com.ubb.tpjad.copygram_posts.entity.Post;
import com.ubb.tpjad.copygram_posts.entity.PostLike;
import com.ubb.tpjad.copygram_posts.mapper.PostMapper;
import com.ubb.tpjad.copygram_posts.repository.PostLikeRepository;
import com.ubb.tpjad.copygram_posts.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ubb.tpjad.copygram_posts.testutil.TestDataBuilder.*;
import static com.ubb.tpjad.copygram_posts.testutil.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private RemotePhotoClient photoClient;

    @Mock
    private CommentService commentService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostMapper mapper;

    @InjectMocks
    private PostService postService;

    // ==================== createPost() Tests ====================

    @Test
    void createPost_Success_ReturnsPostDto() {
        // Given
        MultipartFile photo = buildMultipartFile();
        PhotoMetadataDto photoMetadata = buildPhotoMetadataDto(TEST_PHOTO_ID);
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        PostDto expectedDto = buildPostDto(TEST_POST_ID, TEST_USER_ID);

        when(photoClient.uploadPostPhoto(photo, TEST_DESCRIPTION, TEST_USER_ID))
                .thenReturn(ResponseEntity.ok(photoMetadata));
        when(mapper.map(TEST_USER_ID, TEST_PHOTO_ID.toString(), TEST_DESCRIPTION))
                .thenReturn(post);
        when(postRepository.save(post)).thenReturn(post);
        when(mapper.map(post)).thenReturn(expectedDto);

        // When
        PostDto result = postService.createPost(TEST_DESCRIPTION, photo, TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDto);
        verify(photoClient).uploadPostPhoto(photo, TEST_DESCRIPTION, TEST_USER_ID);
        verify(postRepository).save(post);
        verify(mapper).map(post);
    }

    @Test
    void createPost_PhotoUploadReturnsNullBody_ThrowsResponseStatusException() {
        // Given
        MultipartFile photo = buildMultipartFile();

        when(photoClient.uploadPostPhoto(photo, TEST_DESCRIPTION, TEST_USER_ID))
                .thenReturn(ResponseEntity.ok(null));

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.createPost(TEST_DESCRIPTION, photo, TEST_USER_ID)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(exception.getReason()).contains("Photo upload service returned unexpected result");
        verify(postRepository, never()).save(any());
    }

    @Test
    void createPost_PhotoUploadFails_PropagatesException() {
        // Given
        MultipartFile photo = buildMultipartFile();
        ResponseStatusException uploadException = new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Photo service error"
        );

        when(photoClient.uploadPostPhoto(photo, TEST_DESCRIPTION, TEST_USER_ID))
                .thenThrow(uploadException);

        // When & Then
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> postService.createPost(TEST_DESCRIPTION, photo, TEST_USER_ID)
        );

        assertThat(exception).isEqualTo(uploadException);
        verify(postRepository, never()).save(any());
    }

    // ==================== getPostMetadata() Tests ====================

    @Test
    void getPostMetadata_ValidPostId_ReturnsMetadataWithLikesAndComments() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        long likesCount = 5L;
        List<PostCommentDto> comments = List.of(
                buildPostCommentDto(TEST_COMMENT_ID, TEST_POST_ID, TEST_USER_ID),
                buildPostCommentDto(TEST_COMMENT_ID_2, TEST_POST_ID, TEST_USER_ID_2)
        );
        PostMetadataDto expectedMetadata = PostMetadataDto.builder()
                .postId(TEST_POST_ID)
                .likesCount(likesCount)
                .comments(comments)
                .build();

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPost_Id(TEST_POST_ID)).thenReturn(likesCount);
        when(commentService.getPostCommentsWithMetadata(TEST_POST_ID)).thenReturn(comments);
        when(mapper.map(post, likesCount, comments)).thenReturn(expectedMetadata);

        // When
        PostMetadataDto result = postService.getPostMetadata(TEST_POST_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.likesCount()).isEqualTo(likesCount);
        assertThat(result.comments()).hasSize(2);
        verify(postRepository).findById(TEST_POST_ID);
        verify(postLikeRepository).countByPost_Id(TEST_POST_ID);
        verify(commentService).getPostCommentsWithMetadata(TEST_POST_ID);
    }

    @Test
    void getPostMetadata_InvalidPostId_ThrowsInvalidPostException() {
        // Given
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        // When & Then
        InvalidPostException exception = assertThrows(
                InvalidPostException.class,
                () -> postService.getPostMetadata(TEST_POST_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        verify(postLikeRepository, never()).countByPost_Id(any());
        verify(commentService, never()).getPostCommentsWithMetadata(any());
    }

    @Test
    void getPostMetadata_PostWithNoLikesOrComments_ReturnsMetadataWithZeros() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);
        long likesCount = 0L;
        List<PostCommentDto> comments = Collections.emptyList();
        PostMetadataDto expectedMetadata = PostMetadataDto.builder()
                .postId(TEST_POST_ID)
                .likesCount(likesCount)
                .comments(comments)
                .build();

        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPost_Id(TEST_POST_ID)).thenReturn(likesCount);
        when(commentService.getPostCommentsWithMetadata(TEST_POST_ID)).thenReturn(comments);
        when(mapper.map(post, likesCount, comments)).thenReturn(expectedMetadata);

        // When
        PostMetadataDto result = postService.getPostMetadata(TEST_POST_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.likesCount()).isZero();
        assertThat(result.comments()).isEmpty();
    }

    // ==================== getPostsByUserId() Tests ====================

    @Test
    void getPostsByUserId_UserHasPosts_ReturnsUserPostsResponse() {
        // Given
        Post post1 = buildPost(TEST_POST_ID, TEST_USER_ID);
        Post post2 = buildPost(TEST_POST_ID_2, TEST_USER_ID);
        Post post3 = buildPost(TEST_POST_ID_3, TEST_USER_ID);
        List<Post> posts = List.of(post1, post2, post3);

        PostDto dto1 = buildPostDto(TEST_POST_ID, TEST_USER_ID);
        PostDto dto2 = buildPostDto(TEST_POST_ID_2, TEST_USER_ID);
        PostDto dto3 = buildPostDto(TEST_POST_ID_3, TEST_USER_ID);

        when(postRepository.findByUserId(TEST_USER_ID)).thenReturn(posts);
        when(mapper.map(post1)).thenReturn(dto1);
        when(mapper.map(post2)).thenReturn(dto2);
        when(mapper.map(post3)).thenReturn(dto3);

        // When
        UserPostsResponse result = postService.getPostsByUserId(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(TEST_USER_ID);
        assertThat(result.posts()).hasSize(3);
        assertThat(result.posts()).containsExactly(dto1, dto2, dto3);
        verify(mapper, times(3)).map(any(Post.class));
    }

    @Test
    void getPostsByUserId_UserHasNoPosts_ReturnsEmptyList() {
        // Given
        when(postRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

        // When
        UserPostsResponse result = postService.getPostsByUserId(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(TEST_USER_ID);
        assertThat(result.posts()).isEmpty();
        verify(mapper, never()).map(any(Post.class));
    }

    // ==================== getRandomPosts() Tests ====================

    @Test
    void getRandomPosts_ValidLimit_ReturnsRandomPosts() {
        // Given
        int limit = 10;
        List<Post> posts = List.of(
                buildPost(TEST_POST_ID, TEST_USER_ID),
                buildPost(TEST_POST_ID_2, TEST_USER_ID_2)
        );
        List<PostDto> expectedDtos = List.of(
                buildPostDto(TEST_POST_ID, TEST_USER_ID),
                buildPostDto(TEST_POST_ID_2, TEST_USER_ID_2)
        );

        when(postRepository.findRandomPosts(limit)).thenReturn(posts);
        when(mapper.map(posts.get(0))).thenReturn(expectedDtos.get(0));
        when(mapper.map(posts.get(1))).thenReturn(expectedDtos.get(1));

        // When
        List<PostDto> result = postService.getRandomPosts(limit);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedDtos);
        verify(postRepository).findRandomPosts(limit);
    }

    @Test
    void getRandomPosts_LimitExceedsAvailable_ReturnsAvailablePosts() {
        // Given
        int limit = 100;
        List<Post> posts = List.of(
                buildPost(TEST_POST_ID, TEST_USER_ID)
        );
        List<PostDto> expectedDtos = List.of(
                buildPostDto(TEST_POST_ID, TEST_USER_ID)
        );

        when(postRepository.findRandomPosts(limit)).thenReturn(posts);
        when(mapper.map(posts.get(0))).thenReturn(expectedDtos.get(0));

        // When
        List<PostDto> result = postService.getRandomPosts(limit);

        // Then
        assertThat(result).hasSize(1);
        verify(postRepository).findRandomPosts(limit);
    }

    @Test
    void getRandomPosts_NoPostsAvailable_ReturnsEmptyList() {
        // Given
        int limit = 10;
        when(postRepository.findRandomPosts(limit)).thenReturn(Collections.emptyList());

        // When
        List<PostDto> result = postService.getRandomPosts(limit);

        // Then
        assertThat(result).isEmpty();
        verify(postRepository).findRandomPosts(limit);
    }

    // ==================== getPhotoIdForPost() Tests ====================

    @Test
    void getPhotoIdForPost_ValidPostId_ReturnsPhotoId() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID, TEST_PICTURE_ID, TEST_DESCRIPTION);
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(post));

        // When
        String result = postService.getPhotoIdForPost(TEST_POST_ID);

        // Then
        assertThat(result).isEqualTo(TEST_PICTURE_ID);
        verify(postRepository).findById(TEST_POST_ID);
    }

    @Test
    void getPhotoIdForPost_InvalidPostId_ThrowsInvalidPostException() {
        // Given
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        // When & Then
        InvalidPostException exception = assertThrows(
                InvalidPostException.class,
                () -> postService.getPhotoIdForPost(TEST_POST_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
    }

    // ==================== deletePost() Tests ====================

    @Test
    void deletePost_AuthorizedUser_DeletesPost() {
        // Given
        when(postRepository.existsByIdAndUserId(TEST_POST_ID, TEST_USER_ID)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> postService.deletePost(TEST_POST_ID, TEST_USER_ID));

        // Then
        verify(postRepository).existsByIdAndUserId(TEST_POST_ID, TEST_USER_ID);
        verify(postRepository).deleteById(TEST_POST_ID);
    }

    @Test
    void deletePost_UnauthorizedUser_ThrowsUnauthorizedDeletionException() {
        // Given
        when(postRepository.existsByIdAndUserId(TEST_POST_ID, TEST_USER_ID_2)).thenReturn(false);

        // When & Then
        UnauthorizedDeletionException exception = assertThrows(
                UnauthorizedDeletionException.class,
                () -> postService.deletePost(TEST_POST_ID, TEST_USER_ID_2)
        );

        assertThat(exception.getMessage()).contains(TEST_USER_ID_2);
        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        verify(postRepository, never()).deleteById(any());
    }

    @Test
    void deletePost_NonExistentPost_ThrowsUnauthorizedDeletionException() {
        // Given
        when(postRepository.existsByIdAndUserId(TEST_POST_ID, TEST_USER_ID)).thenReturn(false);

        // When & Then
        UnauthorizedDeletionException exception = assertThrows(
                UnauthorizedDeletionException.class,
                () -> postService.deletePost(TEST_POST_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        verify(postRepository, never()).deleteById(any());
    }

    // ==================== postLike() Tests ====================

    @Test
    void postLike_ValidRequest_CreatesLike() {
        // Given
        Post post = buildPost(TEST_POST_ID, TEST_USER_ID);

        when(postLikeRepository.existsByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID)).thenReturn(false);
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.of(post));

        // When
        assertDoesNotThrow(() -> postService.postLike(TEST_POST_ID, TEST_USER_ID));

        // Then
        ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
        verify(postLikeRepository).save(captor.capture());

        PostLike savedLike = captor.getValue();
        assertThat(savedLike.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(savedLike.getPost()).isEqualTo(post);
    }

    @Test
    void postLike_DuplicateLike_ThrowsInvalidLikeActionException() {
        // Given
        when(postLikeRepository.existsByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID)).thenReturn(true);

        // When & Then
        InvalidLikeActionException exception = assertThrows(
                InvalidLikeActionException.class,
                () -> postService.postLike(TEST_POST_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        assertThat(exception.getMessage()).contains(TEST_USER_ID);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void postLike_InvalidPostId_ThrowsInvalidPostException() {
        // Given
        when(postLikeRepository.existsByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID)).thenReturn(false);
        when(postRepository.findById(TEST_POST_ID)).thenReturn(Optional.empty());

        // When & Then
        InvalidPostException exception = assertThrows(
                InvalidPostException.class,
                () -> postService.postLike(TEST_POST_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        verify(postLikeRepository, never()).save(any());
    }

    // ==================== postUnlike() Tests ====================

    @Test
    void postUnlike_ValidRequest_RemovesLike() {
        // Given
        when(postLikeRepository.existsByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> postService.postUnlike(TEST_POST_ID, TEST_USER_ID));

        // Then
        verify(postLikeRepository).existsByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID);
        verify(postLikeRepository).deleteByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID);
    }

    @Test
    void postUnlike_NoExistingLike_ThrowsInvalidLikeActionException() {
        // Given
        when(postLikeRepository.existsByUserIdAndPost_Id(TEST_USER_ID, TEST_POST_ID)).thenReturn(false);

        // When & Then
        InvalidLikeActionException exception = assertThrows(
                InvalidLikeActionException.class,
                () -> postService.postUnlike(TEST_POST_ID, TEST_USER_ID)
        );

        assertThat(exception.getMessage()).contains(TEST_POST_ID);
        assertThat(exception.getMessage()).contains(TEST_USER_ID);
        verify(postLikeRepository, never()).deleteByUserIdAndPost_Id(any(), any());
    }
}
