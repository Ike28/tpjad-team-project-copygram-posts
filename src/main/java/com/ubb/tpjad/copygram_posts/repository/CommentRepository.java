package com.ubb.tpjad.copygram_posts.repository;

import com.ubb.tpjad.copygram_posts.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByPost_Id(String postId);

    boolean existsByIdAndUserId(String commentId, String userId);
}
