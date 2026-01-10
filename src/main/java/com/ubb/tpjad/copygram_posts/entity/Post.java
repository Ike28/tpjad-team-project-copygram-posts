package com.ubb.tpjad.copygram_posts.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post")
public class Post {
    @Id
    @Column
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "picture_id")
    private String pictureId;

    @Column(name = "description")
    private String description;

    @Column(name = "likes_count")
    private long likesCount;

    @OneToMany(mappedBy = "post")
    private Set<Comment> postComments;
}
