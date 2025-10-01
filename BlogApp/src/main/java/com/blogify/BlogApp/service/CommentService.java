package com.blogify.BlogApp.service;

import com.blogify.BlogApp.dto.CommentDTO;
import com.blogify.BlogApp.dto.CreateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {
    CommentDTO addComment(Long userId, Long postId, CreateCommentRequest request);
    Page<CommentDTO> getCommentsByPost(Long postId, Pageable pageable);
}
