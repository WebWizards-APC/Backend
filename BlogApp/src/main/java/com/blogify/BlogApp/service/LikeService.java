package com.blogify.BlogApp.service;

import com.blogify.BlogApp.dto.LikeDTO;
import com.blogify.BlogApp.dto.LikeResponseDTO;
import com.blogify.BlogApp.repository.LikeRepository;
import org.springframework.stereotype.Service;

@Service
public interface LikeService {
    LikeDTO likePost(Long userId,Long postId);
    void unlikePost(Long userId,Long postId);
    LikeResponseDTO getLikeCount(Long postId,Long userId);
}
