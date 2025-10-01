package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.LikeDTO;
import com.blogify.BlogApp.dto.LikeResponseDTO;
import com.blogify.BlogApp.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/like")
public class LikeController {
    private final LikeService likeService;
    public LikeController(LikeService likeService){
        this.likeService=likeService;
    }

    @PostMapping
    public ResponseEntity<LikeDTO> like(@PathVariable Long postId, @RequestParam Long userId){
        return ResponseEntity.ok(likeService.likePost(userId,postId));
    }

    @GetMapping
    public ResponseEntity<LikeResponseDTO> getLikeCount(@PathVariable Long postId,@RequestParam(required = false) Long userId){
        return ResponseEntity.ok(likeService.getLikeCount(postId,userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> unlike(@PathVariable Long postId,@RequestParam Long userId){
        likeService.unlikePost(userId,postId);
        return ResponseEntity.noContent().build();
    }
}
