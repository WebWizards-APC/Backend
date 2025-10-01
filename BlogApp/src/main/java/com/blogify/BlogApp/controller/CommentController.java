package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.CommentDTO;
import com.blogify.BlogApp.dto.CreateCommentRequest;
import com.blogify.BlogApp.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long postId, @RequestParam Long userId, @RequestBody @Valid CreateCommentRequest request){
        return ResponseEntity.ok(commentService.addComment(userId,postId,request));
    }

    @GetMapping
    public ResponseEntity<Page<CommentDTO>> getComments(@PathVariable Long postId,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(commentService.getCommentsByPost(postId,pageable));
    }
}
