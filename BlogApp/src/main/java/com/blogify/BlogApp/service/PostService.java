package com.blogify.BlogApp.service;

import com.blogify.BlogApp.dto.CreatePostRequest;
import com.blogify.BlogApp.dto.PostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Service
public interface PostService {
    PostDTO createPost(Long userId, CreatePostRequest request, MultipartFile image);
    Page<PostDTO> getAllPosts(Pageable pageable);
    PostDTO getPostById(Long postId);
    void deletePost(Long postId);
    PostDTO updatePost(Long userId,Long postId,CreatePostRequest request,MultipartFile image);
    Page<PostDTO> getMyBlogs(Long userId, Pageable pageable);
}
