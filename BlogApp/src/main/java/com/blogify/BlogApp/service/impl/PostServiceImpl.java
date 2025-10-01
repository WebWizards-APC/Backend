package com.blogify.BlogApp.service.impl;

import com.blogify.BlogApp.dto.CreatePostRequest;
import com.blogify.BlogApp.dto.PostDTO;
import com.blogify.BlogApp.entity.Post;
import com.blogify.BlogApp.entity.User;
import com.blogify.BlogApp.exception.ResourceNotFoundException;
import com.blogify.BlogApp.exception.UnauthorizedException;
import com.blogify.BlogApp.repository.PostRepository;
import com.blogify.BlogApp.repository.UserRepository;
import com.blogify.BlogApp.service.PostService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final ModelMapper modelMapper;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository, Cloudinary cloudinary, ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
        this.modelMapper = modelMapper;
    }

    // Business Logic for creating a blog:
    @Override
    public PostDTO createPost(Long userId, CreatePostRequest request, MultipartFile image){
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
        String imgUrl = null;
        if(image !=null && !image.isEmpty()){
            try{
                Map uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap("folder","blog_posts")
                );
                imgUrl = (String) uploadResult.get("secure_url");

            } catch (IOException e) {
                throw new RuntimeException("Image upload failed",e);
            }
        }

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImgUrl(imgUrl);
        post.setUser(user);

        Post saved = postRepository.save(post);
        return mapPostToDTO(saved);
    }

    // Business logic for getting all in infinite scroll manner(for improving performance):
    @Override
    public Page<PostDTO> getAllPosts(Pageable pageable){
        return postRepository.findAll(pageable).map(this::mapPostToDTO);
    }

    // Business logic for getting a particular blog using its id:
    @Override
    public PostDTO getPostById(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post not found"));
        return mapPostToDTO(post);
    }

    // Business logic for getting a particular users blogs: // Improve after ETE (Make it more Secure)
    @Override
    public Page<PostDTO> getMyBlogs(Long userId, Pageable pageable){
      userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
      Page<Post> posts = postRepository.findByUserId(userId,pageable);
      return posts.map(this::mapPostToDTO);
    }

    // Business logic for deleting a particular blog:(Need to do improvements later.):
    @Override
    public void deletePost(Long postId){
        if(!postRepository.existsById(postId)){
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.deleteById(postId);
    }

     @Override
    public PostDTO updatePost(Long userId,Long postId,CreatePostRequest request,MultipartFile image){
            User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));
            Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post not found"));
            if (!post.getUser().getId().equals(userId)) {
                   throw new UnauthorizedException("You are not authorized to update this post");
            }
            String imgUrl = post.getImgUrl();
            if(image != null && !image.isEmpty()){
                try{
                    if(imgUrl != null && !imgUrl.isEmpty()){
                        // extract public_id from url
                        String publicId = imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.lastIndexOf("."));
                        cloudinary.uploader().destroy("blog_posts/" + publicId, ObjectUtils.emptyMap());
                    }
                    Map uploadResult = cloudinary.uploader().upload(
                            image.getBytes(),
                            ObjectUtils.asMap("folder","blog_posts")
                    );
                    imgUrl = (String) uploadResult.get("secure_url");
                }catch(IOException e){
                    throw new RuntimeException("Image upload failed",e);
                }
            }
            post.setTitle(request.getTitle()!=null && !request.getTitle().isEmpty()?request.getTitle(): post.getTitle());
            post.setContent(request.getContent()!=null && !request.getContent().isEmpty()?request.getContent(): post.getContent());
            post.setImgUrl(imgUrl);

         Post updated = postRepository.save(post);
         return mapPostToDTO(updated);
     }

    private PostDTO mapPostToDTO(Post post){
        PostDTO dto = modelMapper.map(post,PostDTO.class);
        if(post.getUser() != null){
            dto.setUserId(post.getUser().getId());
            dto.setName(post.getUser().getName());
        }
        dto.setCreatedAt(post.getCreatedAt());
        return dto;
    }

}
