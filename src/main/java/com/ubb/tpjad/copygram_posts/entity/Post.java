package com.ubb.tpjad.copygram_posts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "post")
public class Post extends AuditEntity {
    @Id
    @Column
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "picture_id")
    private String pictureId;

    @Column(name = "description")
    private String description;
}
