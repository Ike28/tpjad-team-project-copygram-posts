package com.ubb.tpjad.copygram_posts.repository;

import com.ubb.tpjad.copygram_posts.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, String> {
    long countByPost_Id(String postId);

    boolean existsByUserIdAndPost_Id(String userId, String postId);
}
