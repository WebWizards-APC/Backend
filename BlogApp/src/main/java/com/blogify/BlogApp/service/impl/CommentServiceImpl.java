package com.blogify.BlogApp.service.impl;

import com.blogify.BlogApp.dto.CommentDTO;
import com.blogify.BlogApp.dto.CreateCommentRequest;
import com.blogify.BlogApp.entity.Comment;
import com.blogify.BlogApp.entity.Post;
import com.blogify.BlogApp.entity.User;
import com.blogify.BlogApp.exception.ResourceNotFoundException;
import com.blogify.BlogApp.repository.CommentRepository;
import com.blogify.BlogApp.repository.PostRepository;
import com.blogify.BlogApp.repository.UserRepository;
import com.blogify.BlogApp.service.CommentService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, PostRepository postRepository, ModelMapper modelMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CommentDTO addComment(Long userId, Long postId, CreateCommentRequest request){
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found."));
        Post post = postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("Post not found."));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setPost(post);

        Comment saved = commentRepository.save(comment);
        return mapCommentToDto(saved);
    }

    @Override
    public Page<CommentDTO> getCommentsByPost(Long postId, Pageable pageable){
        return commentRepository.findByPostId(postId,pageable).map(this::mapCommentToDto);
    }

    private CommentDTO mapCommentToDto(Comment com){
        CommentDTO dto = modelMapper.map(com,CommentDTO.class);
        if(com.getUser() != null){
            dto.setUserId(com.getUser().getId());
            dto.setUserName(com.getUser().getName());
        }
        if(com.getPost() != null) dto.setPostId(com.getPost().getId());
        dto.setCreatedAt(com.getCreatedAt());
        return dto;
    }
}
