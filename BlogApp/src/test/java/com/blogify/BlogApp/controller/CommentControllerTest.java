package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.CommentDTO;
import com.blogify.BlogApp.dto.CreateCommentRequest;
import com.blogify.BlogApp.service.CommentService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommentController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@DisplayName("Comment Controller Tests")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CommentDTO commentDTO;
    private CreateCommentRequest createCommentRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setContent("This is a test comment");
        commentDTO.setUserId(1L);
        commentDTO.setPostId(1L);
        commentDTO.setUserName("Test User");
        commentDTO.setCreatedAt(LocalDateTime.now());

        createCommentRequest = new CreateCommentRequest();
        createCommentRequest.setContent("This is a test comment");
    }

    @Nested
    @DisplayName("Add Comment Tests")
    class AddCommentTests {

        @Test
        @DisplayName("Should add comment successfully with valid data")
        void shouldAddCommentSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            when(commentService.addComment(eq(userId), eq(postId), any(CreateCommentRequest.class)))
                    .thenReturn(commentDTO);

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.content").value("This is a test comment"))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.postId").value(1L))
                    .andExpect(jsonPath("$.userName").value("Test User"));

            ArgumentCaptor<CreateCommentRequest> requestCaptor = ArgumentCaptor.forClass(CreateCommentRequest.class);
            verify(commentService, times(1)).addComment(eq(userId), eq(postId), requestCaptor.capture());

            CreateCommentRequest capturedRequest = requestCaptor.getValue();
            assertEquals("This is a test comment", capturedRequest.getContent());
        }

        @Test
        @DisplayName("Should return bad request for empty content")
        void shouldReturnBadRequestForEmptyContent() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            CreateCommentRequest invalidRequest = new CreateCommentRequest();
            invalidRequest.setContent("");

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).addComment(anyLong(), anyLong(), any(CreateCommentRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for null content")
        void shouldReturnBadRequestForNullContent() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            CreateCommentRequest invalidRequest = new CreateCommentRequest();
            invalidRequest.setContent(null);

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).addComment(anyLong(), anyLong(), any(CreateCommentRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for missing userId")
        void shouldReturnBadRequestForMissingUserId() throws Exception {
            // Given
            Long postId = 1L;

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentRequest)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(commentService, never()).addComment(anyLong(), anyLong(), any(CreateCommentRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for missing request body")
        void shouldReturnBadRequestForMissingRequestBody() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(commentService, never()).addComment(anyLong(), anyLong(), any(CreateCommentRequest.class));
        }

        @Test
        @DisplayName("Should handle service exception during comment creation")
        void shouldHandleServiceExceptionDuringCommentCreation() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            when(commentService.addComment(eq(userId), eq(postId), any(CreateCommentRequest.class)))
                    .thenThrow(new RuntimeException("Post not found"));

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentRequest)))
                    .andExpect(status().isInternalServerError());

            verify(commentService, times(1)).addComment(eq(userId), eq(postId), any(CreateCommentRequest.class));
        }
    }

    @Nested
    @DisplayName("Get Comments Tests")
    class GetCommentsTests {

        @Test
        @DisplayName("Should get comments with default pagination")
        void shouldGetCommentsWithDefaultPagination() throws Exception {
            // Given
            Long postId = 1L;
            List<CommentDTO> commentList = Arrays.asList(commentDTO);
            Page<CommentDTO> commentPage = new PageImpl<>(commentList, PageRequest.of(0, 10, Sort.by("createdAt").descending()), 1);
            when(commentService.getCommentsByPost(eq(postId), any(Pageable.class))).thenReturn(commentPage);

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].content").value("This is a test comment"))
                    .andExpect(jsonPath("$.content[0].userId").value(1L))
                    .andExpect(jsonPath("$.content[0].postId").value(1L))
                    .andExpect(jsonPath("$.content[0].userName").value("Test User"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(commentService, times(1)).getCommentsByPost(eq(postId), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(0, capturedPageable.getPageNumber());
            assertEquals(10, capturedPageable.getPageSize());
            assertEquals(Sort.by("createdAt").descending(), capturedPageable.getSort());
        }

        @Test
        @DisplayName("Should get comments with custom pagination")
        void shouldGetCommentsWithCustomPagination() throws Exception {
            // Given
            Long postId = 1L;
            List<CommentDTO> commentList = Arrays.asList(commentDTO);
            Page<CommentDTO> commentPage = new PageImpl<>(commentList, PageRequest.of(2, 5, Sort.by("createdAt").descending()), 1);
            when(commentService.getCommentsByPost(eq(postId), any(Pageable.class))).thenReturn(commentPage);

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                            .param("page", "2")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(2));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(commentService, times(1)).getCommentsByPost(eq(postId), pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(2, capturedPageable.getPageNumber());
            assertEquals(5, capturedPageable.getPageSize());
            assertEquals(Sort.by("createdAt").descending(), capturedPageable.getSort());
        }

        @Test
        @DisplayName("Should handle empty comments list")
        void shouldHandleEmptyCommentsList() throws Exception {
            // Given
            Long postId = 1L;
            Page<CommentDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10, Sort.by("createdAt").descending()), 0);
            when(commentService.getCommentsByPost(eq(postId), any(Pageable.class))).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(commentService, times(1)).getCommentsByPost(eq(postId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle service exception during comments retrieval")
        void shouldHandleServiceExceptionDuringCommentsRetrieval() throws Exception {
            // Given
            Long postId = 999L;
            when(commentService.getCommentsByPost(eq(postId), any(Pageable.class)))
                    .thenThrow(new RuntimeException("Post not found"));

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}/comments", postId))
                    .andExpect(status().isInternalServerError());

            verify(commentService, times(1)).getCommentsByPost(eq(postId), any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle invalid pagination parameters")
        void shouldHandleInvalidPaginationParameters() throws Exception {
            // Given
            Long postId = 1L;

            // When & Then
            mockMvc.perform(get("/api/posts/{postId}/comments", postId)
                            .param("page", "-1")
                            .param("size", "0"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Controller Integration Tests")
    class ControllerIntegrationTests {

        @Test
        @DisplayName("Should handle invalid postId path variable")
        void shouldHandleInvalidPostIdPathVariable() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/posts/invalid/comments"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJsonInRequestBody() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(commentService, never()).addComment(anyLong(), anyLong(), any(CreateCommentRequest.class));
        }

        @Test
        @DisplayName("Should verify controller autowiring")
        void shouldVerifyControllerAutowiring() {
            // Then
            assertNotNull(mockMvc);
            assertNotNull(commentService);
            assertNotNull(objectMapper);
        }

        @Test
        @DisplayName("Should handle different postId values")
        void shouldHandleDifferentPostIdValues() throws Exception {
            // Given
            Long postId = 999L;
            Long userId = 1L;
            CommentDTO commentForDifferentPost = new CommentDTO();
            commentForDifferentPost.setId(2L);
            commentForDifferentPost.setContent("Comment for different post");
            commentForDifferentPost.setUserId(userId);
            commentForDifferentPost.setPostId(postId);
            commentForDifferentPost.setUserName("Another User");
            commentForDifferentPost.setCreatedAt(LocalDateTime.now());

            when(commentService.addComment(eq(userId), eq(postId), any(CreateCommentRequest.class)))
                    .thenReturn(commentForDifferentPost);

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.postId").value(postId))
                    .andExpect(jsonPath("$.userName").value("Another User"));

            verify(commentService, times(1)).addComment(eq(userId), eq(postId), any(CreateCommentRequest.class));
        }

        @Test
        @DisplayName("Should handle validation error with detailed message")
        void shouldHandleValidationErrorWithDetailedMessage() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            CreateCommentRequest requestWithWhitespace = new CreateCommentRequest();
            requestWithWhitespace.setContent("   "); // Only whitespace

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                            .param("userId", userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithWhitespace)))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).addComment(anyLong(), anyLong(), any(CreateCommentRequest.class));
        }
    }
}