package com.ubb.tpjad.copygram_posts.mapper;

import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.entity.Comment;
import com.ubb.tpjad.copygram_posts.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentMapper {
    private final IdMapper idMapper;

    public PostCommentDto map(Comment comment, long likesCount) {
        return PostCommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUserId())
                .text(comment.getText())
                .likesCount(likesCount)
                .build();
    }

    public Comment map(PostCommentDto commentDto, Post post) {
        return Comment.builder()
                .id(idMapper.mapId(commentDto.id()))
                .userId(commentDto.userId())
                .post(post)
                .text(commentDto.text())
                .build();
    }
}
