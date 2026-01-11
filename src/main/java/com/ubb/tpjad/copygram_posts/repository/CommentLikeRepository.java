package com.ubb.tpjad.copygram_posts.repository;

import com.ubb.tpjad.copygram_posts.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, String> {
    long countByComment_Id(String commentId);

    boolean existsByUserIdAndComment_Id(String userId, String commentId);

    void deleteByUserIdAndComment_Id(String userId, String commentId);
}
