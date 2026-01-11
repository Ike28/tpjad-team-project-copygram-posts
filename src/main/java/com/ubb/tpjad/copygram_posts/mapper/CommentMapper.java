package com.ubb.tpjad.copygram_posts.mapper;

import com.ubb.tpjad.copygram_posts.api.dto.PostCommentDto;
import com.ubb.tpjad.copygram_posts.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    public PostCommentDto map(Comment comment, long likesCount) {
        return PostCommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .userId(comment.getUserId())
                .text(comment.getText())
                .likesCount(likesCount)
                .build();
    }
}
