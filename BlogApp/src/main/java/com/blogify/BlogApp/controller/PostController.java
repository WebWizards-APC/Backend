package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.CreatePostRequest;
import com.blogify.BlogApp.dto.PostDTO;
import com.blogify.BlogApp.service.PostService;
import com.blogify.BlogApp.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    public PostController(PostService postService,UserService userService) {
        this.postService = postService;
        this.userService= userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(@RequestParam Long userId, @RequestParam String title, @RequestParam String content, @RequestPart(value = "image",required = false)MultipartFile image){
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle(title);
        req.setContent(content);

        PostDTO dto = postService.createPost(userId,req,image);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ){
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getById(@PathVariable Long postId){
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/my-blogs")
    public ResponseEntity<Page<PostDTO>> getAllMyBlogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size,@RequestParam Long userId){
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(postService.getMyBlogs(userId,pageable));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(@PathVariable Long postId){
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(path = "/{postId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long postId,@RequestParam Long userId,@RequestParam String title, @RequestParam String content, @RequestPart(value = "image",required = false)MultipartFile image){
        CreatePostRequest req = new CreatePostRequest();
        req.setTitle(title);
        req.setContent(content);
        PostDTO dto = postService.updatePost(userId,postId,req,image);
        return ResponseEntity.ok(dto);
    }
}
