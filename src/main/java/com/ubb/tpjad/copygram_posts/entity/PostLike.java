package com.ubb.tpjad.copygram_posts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "post_like")
public class PostLike extends UserLike {
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_like_post")
    )
    private Post post;
}
