package com.ubb.tpjad.copygram_posts.repository;

import com.ubb.tpjad.copygram_posts.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    boolean existsByIdAndUserId(String postId, String userId);

    List<Post> findByUserId(String userId);
}
