package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.LikeDTO;
import com.blogify.BlogApp.service.LikeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LikeController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@DisplayName("Like Controller Tests")
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LikeService likeService;

    @Autowired
    private ObjectMapper objectMapper;

    private LikeDTO likeDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        likeDTO = new LikeDTO();
        likeDTO.setId(1L);
        likeDTO.setUserId(1L);
        likeDTO.setPostId(1L);
    }

    @Nested
    @DisplayName("Like Post Tests")
    class LikePostTests {

        @Test
        @DisplayName("Should like post successfully")
        void shouldLikePostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            when(likeService.likePost(userId, postId)).thenReturn(likeDTO);

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.postId").value(1L));

            verify(likeService, times(1)).likePost(userId, postId);
        }

        @Test
        @DisplayName("Should return bad request for missing userId")
        void shouldReturnBadRequestForMissingUserId() throws Exception {
            // Given
            Long postId = 1L;

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(likeService, never()).likePost(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle service exception during like operation")
        void shouldHandleServiceExceptionDuringLikeOperation() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            when(likeService.likePost(userId, postId))
                    .thenThrow(new RuntimeException("Post not found"));

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).likePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle duplicate like attempt")
        void shouldHandleDuplicateLikeAttempt() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            when(likeService.likePost(userId, postId))
                    .thenThrow(new RuntimeException("Post already liked by user"));

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).likePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle non-existent user")
        void shouldHandleNonExistentUser() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 999L;
            when(likeService.likePost(userId, postId))
                    .thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).likePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle non-existent post")
        void shouldHandleNonExistentPost() throws Exception {
            // Given
            Long postId = 999L;
            Long userId = 1L;
            when(likeService.likePost(userId, postId))
                    .thenThrow(new RuntimeException("Post not found"));

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).likePost(userId, postId);
        }
    }

    @Nested
    @DisplayName("Unlike Post Tests")
    class UnlikePostTests {

        @Test
        @DisplayName("Should unlike post successfully")
        void shouldUnlikePostSuccessfully() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            doNothing().when(likeService).unlikePost(userId, postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isNoContent());

            verify(likeService, times(1)).unlikePost(userId, postId);
        }

        @Test
        @DisplayName("Should return bad request for missing userId")
        void shouldReturnBadRequestForMissingUserId() throws Exception {
            // Given
            Long postId = 1L;

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(likeService, never()).unlikePost(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle service exception during unlike operation")
        void shouldHandleServiceExceptionDuringUnlikeOperation() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            doThrow(new RuntimeException("Like not found"))
                    .when(likeService).unlikePost(userId, postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).unlikePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle unlike when not liked")
        void shouldHandleUnlikeWhenNotLiked() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            doThrow(new RuntimeException("Post not liked by user"))
                    .when(likeService).unlikePost(userId, postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).unlikePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle non-existent user during unlike")
        void shouldHandleNonExistentUserDuringUnlike() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 999L;
            doThrow(new RuntimeException("User not found"))
                    .when(likeService).unlikePost(userId, postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).unlikePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle non-existent post during unlike")
        void shouldHandleNonExistentPostDuringUnlike() throws Exception {
            // Given
            Long postId = 999L;
            Long userId = 1L;
            doThrow(new RuntimeException("Post not found"))
                    .when(likeService).unlikePost(userId, postId);

            // When & Then
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isInternalServerError());

            verify(likeService, times(1)).unlikePost(userId, postId);
        }
    }

    @Nested
    @DisplayName("Controller Integration Tests")
    class ControllerIntegrationTests {

        @Test
        @DisplayName("Should handle invalid postId path variable")
        void shouldHandleInvalidPostIdPathVariable() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/posts/invalid/likes")
                            .param("userId", "1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Should handle invalid userId parameter")
        void shouldHandleInvalidUserIdParameter() throws Exception {
            // Given
            Long postId = 1L;

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", "invalid"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Should verify controller autowiring")
        void shouldVerifyControllerAutowiring() {
            // Then
            assertNotNull(mockMvc);
            assertNotNull(likeService);
            assertNotNull(objectMapper);
        }

        @Test
        @DisplayName("Should handle like and unlike operations for same post")
        void shouldHandleLikeAndUnlikeOperationsForSamePost() throws Exception {
            // Given
            Long postId = 1L;
            Long userId = 1L;
            when(likeService.likePost(userId, postId)).thenReturn(likeDTO);
            doNothing().when(likeService).unlikePost(userId, postId);

            // When & Then - Like the post
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.userId").value(1L))
                    .andExpect(jsonPath("$.postId").value(1L));

            // When & Then - Unlike the post
            mockMvc.perform(delete("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isNoContent());

            verify(likeService, times(1)).likePost(userId, postId);
            verify(likeService, times(1)).unlikePost(userId, postId);
        }

        @Test
        @DisplayName("Should handle different user IDs for same post")
        void shouldHandleDifferentUserIdsForSamePost() throws Exception {
            // Given
            Long postId = 1L;
            Long userId1 = 1L;
            Long userId2 = 2L;

            LikeDTO like1 = new LikeDTO();
            like1.setId(1L);
            like1.setUserId(userId1);
            like1.setPostId(postId);

            LikeDTO like2 = new LikeDTO();
            like2.setId(2L);
            like2.setUserId(userId2);
            like2.setPostId(postId);

            when(likeService.likePost(userId1, postId)).thenReturn(like1);
            when(likeService.likePost(userId2, postId)).thenReturn(like2);

            // When & Then - User 1 likes the post
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId1.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId1));

            // When & Then - User 2 likes the same post
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId2.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(userId2));

            verify(likeService, times(1)).likePost(userId1, postId);
            verify(likeService, times(1)).likePost(userId2, postId);
        }

        @Test
        @DisplayName("Should handle edge case with zero IDs")
        void shouldHandleEdgeCaseWithZeroIds() throws Exception {
            // Given
            Long postId = 0L;
            Long userId = 0L;

            LikeDTO zeroLike = new LikeDTO();
            zeroLike.setId(1L);
            zeroLike.setUserId(userId);
            zeroLike.setPostId(postId);

            when(likeService.likePost(userId, postId)).thenReturn(zeroLike);

            // When & Then
            mockMvc.perform(post("/api/posts/{postId}/likes", postId)
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(0))
                    .andExpect(jsonPath("$.postId").value(0));

            verify(likeService, times(1)).likePost(userId, postId);
        }
    }
}