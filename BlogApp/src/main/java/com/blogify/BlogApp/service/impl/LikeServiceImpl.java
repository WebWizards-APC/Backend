package com.blogify.BlogApp.service.impl;

import com.blogify.BlogApp.dto.LikeDTO;
import com.blogify.BlogApp.dto.LikeResponseDTO;
import com.blogify.BlogApp.entity.Like;
import com.blogify.BlogApp.entity.Post;
import com.blogify.BlogApp.entity.User;
import com.blogify.BlogApp.exception.BadRequestException;
import com.blogify.BlogApp.exception.ResourceNotFoundException;
import com.blogify.BlogApp.repository.LikeRepository;
import com.blogify.BlogApp.repository.PostRepository;
import com.blogify.BlogApp.repository.UserRepository;
import com.blogify.BlogApp.service.LikeService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public LikeServiceImpl(LikeRepository likeRepository, UserRepository userRepository, PostRepository postRepository, ModelMapper modelMapper) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public LikeDTO likePost(Long userId,Long postId){
        if (userId == null) {
            throw new RuntimeException("You must be logged in to like a post");
        }

        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found."));
        Post post = postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("Post not found."));

        if (likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new BadRequestException("User already liked this post.");
        }

        Like like = new Like(user,post);
        Like saved = likeRepository.save(like);


        LikeDTO dto = modelMapper.map(saved,LikeDTO.class);
        dto.setUserId(userId);
        dto.setPostId(postId);
        return dto;
    }

    @Override
    @Transactional
    public void unlikePost(Long userId, Long postId) {
        if (!likeRepository.existsByUserIdAndPostId(userId, postId)) {
            throw new ResourceNotFoundException("Like not found");
        }
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    public LikeResponseDTO getLikeCount(Long postId,Long userId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found.");
        }

        boolean likedByUser = false;
        if (userId != null) {
            likedByUser = likeRepository.existsByUserIdAndPostId(userId, postId);
        }
        Long totalLikes = likeRepository.countByPostId(postId);

        return new LikeResponseDTO(postId,totalLikes,likedByUser);
    }
}
