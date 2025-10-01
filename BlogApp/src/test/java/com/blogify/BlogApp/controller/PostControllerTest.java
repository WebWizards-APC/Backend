package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.CreatePostRequest;
import com.blogify.BlogApp.dto.PostDTO;
import com.blogify.BlogApp.service.PostService;
import com.blogify.BlogApp.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@DisplayName("Post Controller Tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostDTO postDTO;
    private CreatePostRequest createPostRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        postDTO = new PostDTO();
        postDTO.setId(1L);
        postDTO.setTitle("Test Post Title");
        postDTO.setContent("Test post content");
        postDTO.setImgUrl("https://example.com/image.jpg");
        postDTO.setUserId(1L);
        postDTO.setName("Test User");
        postDTO.setCreatedAt(LocalDateTime.now());

        createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent("Test post content");
    }

    @Nested
    @DisplayName("Create Post Tests")
    class CreatePostTests {

        @Test
        @DisplayName("Should create post successfully with image")
        void shouldCreatePostSuccessfullyWithImage() throws Exception {
            // Given
            Long userId = 1L;
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "test image content".getBytes()
            );

            when(postService.createPost(eq(userId), any(CreatePostRequest.class), any(MultipartFile.class)))
                    .thenReturn(postDTO);

            // When & Then
            mockMvc.perform(multipart("/api/posts")
                            .file(image)
                            .param("userId", userId.toString())
                            .param("title", "Test Post Title")
                            .param("content", "Test post content"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Test Post Title"))
                    .andExpect(jsonPath("$.content").value("Test post content"))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.name").value("Test User"))
                    .andExpect(jsonPath("$.imgUrl").value("https://example.com/image.jpg"));

            ArgumentCaptor<CreatePostRequest> requestCaptor = ArgumentCaptor.forClass(CreatePostRequest.class);
            verify(postService, times(1)).createPost(eq(userId), requestCaptor.capture(), any(MultipartFile.class));

            CreatePostRequest capturedRequest = requestCaptor.getValue();
            assertEquals("Test Post Title", capturedRequest.getTitle());
            assertEquals("Test post content", capturedRequest.getContent());
        }

        @Test
        @DisplayName("Should create post successfully without image")
        void shouldCreatePostSuccessfullyWithoutImage() throws Exception {
            // Given
            Long userId = 1L;
            when(postService.createPost(eq(userId), any(CreatePostRequest.class), isNull()))
                    .thenReturn(postDTO);

            // When & Then
            mockMvc.perform(multipart("/api/posts")
                            .param("userId", userId.toString())
                            .param("title", "Test Post Title")
                            .param("content", "Test post content"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Test Post Title"))
                    .andExpect(jsonPath("$.content").value("Test post content"));

            verify(postService, times(1)).createPost(eq(userId), any(CreatePostRequest.class), isNull());
        }

        @Test
        @DisplayName("Should return bad request for missing title")
        void shouldReturnBadRequestForMissingTitle() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(multipart("/api/posts")
                            .param("userId", userId.toString())
                            .param("content", "Test post content"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(postService, never()).createPost(anyLong(), any(CreatePostRequest.class), any());
        }

        @Test
        @DisplayName("Should return bad request for missing content")
        void shouldReturnBadRequestForMissingContent() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(multipart("/api/posts")
                            .param("userId", userId.toString())
                            .param("title", "Test Post Title"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(postService, never()).createPost(anyLong(), any(CreatePostRequest.class), any());
        }

        @Test
        @DisplayName("Should return bad request for missing userId")
        void shouldReturnBadRequestForMissingUserId() throws Exception {
            // When & Then
            mockMvc.perform(multipart("/api/posts")
                            .param("title", "Test Post Title")
                            .param("content", "Test post content"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(postService, never()).createPost(anyLong(), any(CreatePostRequest.class), any());
        }
    }

    @Nested
    @DisplayName("Get All Posts Tests")
    class GetAllPostsTests {

        @Test
        @DisplayName("Should get all posts with default pagination")
        void shouldGetAllPostsWithDefaultPagination() throws Exception {
            // Given
            List<PostDTO> postList = Arrays.asList(postDTO);
            Page<PostDTO> postPage = new PageImpl<>(postList, PageRequest.of(0, 6, Sort.by("createdAt").descending()), 1);
            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            // When & Then
            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].title").value("Test Post Title"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(6))
                    .andExpect(jsonPath("$.number").value(0));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(postService, times(1)).getAllPosts(pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(0, capturedPageable.getPageNumber());
            assertEquals(6, capturedPageable.getPageSize());
            assertEquals(Sort.by("createdAt").descending(), capturedPageable.getSort());
        }

        @Test
        @DisplayName("Should get all posts with custom pagination")
        void shouldGetAllPostsWithCustomPagination() throws Exception {
            // Given
            List<PostDTO> postList = Arrays.asList(postDTO);
            Page<PostDTO> postPage = new PageImpl<>(postList, PageRequest.of(2, 10, Sort.by("createdAt").descending()), 1);
            when(postService.getAllPosts(any(Pageable.class))).thenReturn(postPage);

            // When & Then
            mockMvc.perform(get("/api/posts")
                            .param("page", "2")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(2));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(postService, times(1)).getAllPosts(pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(2, capturedPageable.getPageNumber());
            assertEquals(10, capturedPageable.getPageSize());
            assertEquals(Sort.by("createdAt").descending(), capturedPageable.getSort());
        }

        @Test
        @DisplayName("Should handle empty post list")
        void shouldHandleEmptyPostList() throws Exception {
            // Given
            Page<PostDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 6, Sort.by("createdAt").descending()), 0);
            when(postService.getAllPosts(any(Pageable.class))).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(postService, times(1)).getAllPosts(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Get Post By ID Tests")
    class GetPostByIdTests {

        @Test
        @DisplayName("Should get post by ID successfully")
        void shouldGetPostByIdSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            when(postService.getPostById(postId)).thenReturn(postDTO);

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Test Post Title"))
                    .andExpect(jsonPath("$.content").value("Test post content"))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.name").value("Test User"));

            verify(postService, times(1)).getPostById(postId);
        }

        @Test
        @DisplayName("Should handle post not found")
        void shouldHandlePostNotFound() throws Exception {
            // Given
            Long postId = 999L;
            when(postService.getPostById(postId)).thenThrow(new RuntimeException("Post not found"));

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}", postId))
                    .andExpect(status().isInternalServerError());

            verify(postService, times(1)).getPostById(postId);
        }
    }

    @Nested
    @DisplayName("Get My Blogs Tests")
    class GetMyBlogsTests {

        @Test
        @DisplayName("Should get my blogs with default pagination")
        void shouldGetMyBlogsWithDefaultPagination() throws Exception {
            // Given
            Long userId = 1L;
            List<PostDTO> postList = Arrays.asList(postDTO);
            Page<PostDTO> postPage = new PageImpl<>(postList, PageRequest.of(0, 6, Sort.by("createdAt").descending()), 1);
            when(postService.getMyBlogs(eq(userId), any(Pageable.class))).thenReturn(postPage);

            // When & Then
            mockMvc.perform(get("/api/posts/my-blogs")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].userId").value(1L))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(6))
                    .andExpect(jsonPath("$.number").value(0));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(postService, times(1)).getMyBlogs(eq(userId), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(0, capturedPageable.getPageNumber());
            assertEquals(6, capturedPageable.getPageSize());
            assertEquals(Sort.by("createdAt").descending(), capturedPageable.getSort());
        }

        @Test
        @DisplayName("Should get my blogs with custom pagination")
        void shouldGetMyBlogsWithCustomPagination() throws Exception {
            // Given
            Long userId = 1L;
            List<PostDTO> postList = Arrays.asList(postDTO);
            Page<PostDTO> postPage = new PageImpl<>(postList, PageRequest.of(1, 3, Sort.by("createdAt").descending()), 1);
            when(postService.getMyBlogs(eq(userId), any(Pageable.class))).thenReturn(postPage);

            // When & Then
            mockMvc.perform(get("/api/posts/my-blogs")
                            .param("userId", userId.toString())
                            .param("page", "1")
                            .param("size", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.size").value(3))
                    .andExpect(jsonPath("$.number").value(1));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(postService, times(1)).getMyBlogs(eq(userId), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(1, capturedPageable.getPageNumber());
            assertEquals(3, capturedPageable.getPageSize());
        }

        @Test
        @DisplayName("Should return bad request for missing userId")
        void shouldReturnBadRequestForMissingUserId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/posts/my-blogs"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(postService, never()).getMyBlogs(anyLong(), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Delete Post Tests")
    class DeletePostTests {

        @Test
        @DisplayName("Should delete post successfully")
        void shouldDeletePostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            doNothing().when(postService).deletePost(postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}", postId))
                    .andExpect(status().isNoContent());

            verify(postService, times(1)).deletePost(postId);
        }

        @Test
        @DisplayName("Should handle service exception during deletion")
        void shouldHandleServiceExceptionDuringDeletion() throws Exception {
            // Given
            Long postId = 1L;
            doThrow(new RuntimeException("Post not found")).when(postService).deletePost(postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}", postId))
                    .andExpect(status().isInternalServerError());

            verify(postService, times(1)).deletePost(postId);
        }
    }

    @Nested
    @DisplayName("Update Post Tests")
    class UpdatePostTests {

        @Test
        @DisplayName("Should update post successfully with image")
        void shouldUpdatePostSuccessfullyWithImage() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "updated.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "updated image content".getBytes()
            );

            PostDTO updatedPost = new PostDTO();
            updatedPost.setId(postId);
            updatedPost.setTitle("Updated Title");
            updatedPost.setContent("Updated content");
            updatedPost.setUserId(userId);
            updatedPost.setImgUrl("https://example.com/updated.jpg");

            when(postService.updatePost(eq(userId), eq(postId), any(CreatePostRequest.class), any(MultipartFile.class)))
                    .thenReturn(updatedPost);

            // When & Then
            mockMvc.perform(multipart("/api/posts/{postId}", postId)
                            .file(image)
                            .param("userId", userId.toString())
                            .param("title", "Updated Title")
                            .param("content", "Updated content")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(postId))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.content").value("Updated content"))
                    .andExpect(jsonPath("$.userId").value(userId))
                    .andExpect(jsonPath("$.imgUrl").value("https://example.com/updated.jpg"));

            ArgumentCaptor<CreatePostRequest> requestCaptor = ArgumentCaptor.forClass(CreatePostRequest.class);
            verify(postService, times(1)).updatePost(eq(userId), eq(postId), requestCaptor.capture(), any(MultipartFile.class));

            CreatePostRequest capturedRequest = requestCaptor.getValue();
            assertEquals("Updated Title", capturedRequest.getTitle());
            assertEquals("Updated content", capturedRequest.getContent());
        }

        @Test
        @DisplayName("Should update post successfully without image")
        void shouldUpdatePostSuccessfullyWithoutImage() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            PostDTO updatedPost = new PostDTO();
            updatedPost.setId(postId);
            updatedPost.setTitle("Updated Title");
            updatedPost.setContent("Updated content");
            updatedPost.setUserId(userId);

            when(postService.updatePost(eq(userId), eq(postId), any(CreatePostRequest.class), isNull()))
                    .thenReturn(updatedPost);

            // When & Then
            mockMvc.perform(multipart("/api/posts/{postId}", postId)
                            .param("userId", userId.toString())
                            .param("title", "Updated Title")
                            .param("content", "Updated content")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(postId))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.content").value("Updated content"));

            verify(postService, times(1)).updatePost(eq(userId), eq(postId), any(CreatePostRequest.class), isNull());
        }

        @Test
        @DisplayName("Should return bad request for missing required parameters")
        void shouldReturnBadRequestForMissingRequiredParameters() throws Exception {
            // Given
            Long postId = 1L;

            // When & Then
            mockMvc.perform(multipart("/api/posts/{postId}", postId)
                            .param("title", "Updated Title")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(postService, never()).updatePost(anyLong(), anyLong(), any(CreatePostRequest.class), any());
        }
    }

    @Nested
    @DisplayName("Controller Integration Tests")
    class ControllerIntegrationTests {

        @Test
        @DisplayName("Should handle invalid path variables")
        void shouldHandleInvalidPathVariables() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/posts/invalid"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Should handle invalid pagination parameters")
        void shouldHandleInvalidPaginationParameters() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/posts")
                            .param("page", "-1")
                            .param("size", "0"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should verify controller autowiring")
        void shouldVerifyControllerAutowiring() {
            // Then
            assertNotNull(mockMvc);
            assertNotNull(postService);
            assertNotNull(userService);
            assertNotNull(objectMapper);
        }

        @Test
        @DisplayName("Should handle large file upload")
        void shouldHandleLargeFileUpload() throws Exception {
            // Given
            Long userId = 1L;
            byte[] largeContent = new byte[1024 * 1024]; // 1MB
            MockMultipartFile largeImage = new MockMultipartFile(
                    "image",
                    "large.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    largeContent
            );

            when(postService.createPost(eq(userId), any(CreatePostRequest.class), any(MultipartFile.class)))
                    .thenReturn(postDTO);

            // When & Then
            mockMvc.perform(multipart("/api/posts")
                            .file(largeImage)
                            .param("userId", userId.toString())
                            .param("title", "Test Post Title")
                            .param("content", "Test post content"))
                    .andExpect(status().isCreated());

            verify(postService, times(1)).createPost(eq(userId), any(CreatePostRequest.class), any(MultipartFile.class));
        }
    }
}