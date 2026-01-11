package com.ubb.tpjad.copygram_posts.mapper;

import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostDto;
import com.ubb.tpjad.copygram_posts.api.dto.PostMetadataDto;
import com.ubb.tpjad.copygram_posts.entity.Post;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostMapper {
    public PostDto map(Post entity) {
        return getBaseBuilder(entity).build();
    }

    public PostDto map(Post entity, long likesCount, List<PostCommentDto> comments) {
        return getBaseBuilder(entity)
                .metadata(new PostMetadataDto(entity.getId(), likesCount, comments))
                .build();
    }

    private PostDto.PostDtoBuilder getBaseBuilder(Post entity) {
        return PostDto.builder()
                .id(entity.getId())
                .pictureId(entity.getPictureId())
                .userId(entity.getUserId())
                .description(entity.getDescription());
    }
}
