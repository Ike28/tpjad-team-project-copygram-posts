package com.ubb.tpjad.copygram_posts.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UserLike extends AuditEntity {
    @Id
    @Column
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;
}
