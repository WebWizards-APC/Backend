package com.blogify.BlogApp.repository;

import com.blogify.BlogApp.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like,Long> {
    boolean existsByUserIdAndPostId(Long userId,Long postId);
    void deleteByUserIdAndPostId(Long userId,Long postId);
    long countByPostId(Long postId);
}

