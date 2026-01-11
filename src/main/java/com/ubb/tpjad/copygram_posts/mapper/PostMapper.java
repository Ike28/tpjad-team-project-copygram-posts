package com.ubb.tpjad.copygram_posts.mapper;

import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import com.ubb.tpjad.copygram_posts.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostMapper {
    private final IdMapper idMapper;

    public PostDto map(Post entity) {
        return PostDto.builder()
                .id(entity.getId())
                .pictureId(entity.getPictureId())
                .userId(entity.getUserId())
                .description(entity.getDescription())
                .build();
    }

    public PostMetadataDto map(Post entity, long likesCount, List<PostCommentDto> comments) {
        return PostMetadataDto.builder()
                .postId(entity.getId())
                .userId(entity.getUserId())
                .pictureId(entity.getPictureId())
                .description(entity.getDescription())
                .likesCount(likesCount)
                .comments(comments)
                .build();
    }

    public Post map(String userId, String pictureId, String description) {
        return Post.builder()
                .id(idMapper.mapId(null))
                .userId(userId)
                .pictureId(pictureId)
                .description(description)
                .build();
    }
}
