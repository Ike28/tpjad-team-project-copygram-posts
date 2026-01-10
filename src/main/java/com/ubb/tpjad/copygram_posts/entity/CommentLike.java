package com.ubb.tpjad.copygram_posts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "comment_like")
public class CommentLike extends UserLike {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "comment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_comment_like_comment")
    )
    private Comment comment;
}
