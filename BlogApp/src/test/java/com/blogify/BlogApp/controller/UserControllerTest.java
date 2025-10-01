package com.blogify.BlogApp.controller;

import com.blogify.BlogApp.dto.CreateUpdateProfileRequest;
import com.blogify.BlogApp.dto.LoginRequest;
import com.blogify.BlogApp.dto.UserDTO;
import com.blogify.BlogApp.dto.UserResponseDTO;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;
    private UserResponseDTO userResponseDTO;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");
        userDTO.setName("Test User");
        userDTO.setBio("Test bio");
        userDTO.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        userDTO.setRoles(roles);

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setEmail("test@example.com");
        userResponseDTO.setName("Test User");
        userResponseDTO.setBio("Test bio");
        userResponseDTO.setRoles(roles);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            when(userService.registerUser(any(UserDTO.class))).thenReturn(userDTO);

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.name").value("Test User"))
                    .andExpect(jsonPath("$.bio").value("Test bio"));

            verify(userService, times(1)).registerUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should return bad request for invalid email")
        void shouldReturnBadRequestForInvalidEmail() throws Exception {
            // Given
            userDTO.setEmail("invalid-email");

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should return bad request for empty name")
        void shouldReturnBadRequestForEmptyName() throws Exception {
            // Given
            userDTO.setName("");

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should return bad request for short password")
        void shouldReturnBadRequestForShortPassword() throws Exception {
            // Given
            userDTO.setPassword("123");

            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userDTO)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any(UserDTO.class));
        }
    }

    @Nested
    @DisplayName("User Login Tests")
    class UserLoginTests {

        @Test
        @DisplayName("Should login user successfully with valid credentials")
        void shouldLoginUserSuccessfully() throws Exception {
            // Given
            when(userService.login(anyString(), anyString())).thenReturn(userResponseDTO);

            // When & Then
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.name").value("Test User"));

            verify(userService, times(1)).login("test@example.com", "password123");
        }

        @Test
        @DisplayName("Should return bad request for empty email in login")
        void shouldReturnBadRequestForEmptyEmailInLogin() throws Exception {
            // Given
            loginRequest.setEmail("");

            // When & Then
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).login(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return bad request for empty password in login")
        void shouldReturnBadRequestForEmptyPasswordInLogin() throws Exception {
            // Given
            loginRequest.setPassword("");

            // When & Then
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).login(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UserUpdateTests {

        @Test
        @DisplayName("Should update user successfully with valid data")
        void shouldUpdateUserSuccessfully() throws Exception {
            // Given
            Long userId = 1L;
            MockMultipartFile profileImage = new MockMultipartFile(
                    "profileImage",
                    "profile.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "image content".getBytes()
            );

            when(userService.updateUser(eq(userId), any(CreateUpdateProfileRequest.class), any(MultipartFile.class)))
                    .thenReturn(userResponseDTO);

            // When & Then
            mockMvc.perform(multipart("/api/users/{id}", userId)
                            .file(profileImage)
                            .param("name", "Updated Name")
                            .param("email", "updated@example.com")
                            .param("bio", "Updated bio")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.name").value("Test User"));

            ArgumentCaptor<CreateUpdateProfileRequest> requestCaptor = ArgumentCaptor.forClass(CreateUpdateProfileRequest.class);
            verify(userService, times(1)).updateUser(eq(userId), requestCaptor.capture(), any(MultipartFile.class));

            CreateUpdateProfileRequest capturedRequest = requestCaptor.getValue();
            assertEquals("Updated Name", capturedRequest.getName());
            assertEquals("updated@example.com", capturedRequest.getEmail());
            assertEquals("Updated bio", capturedRequest.getBio());
        }

        @Test
        @DisplayName("Should update user without profile image")
        void shouldUpdateUserWithoutProfileImage() throws Exception {
            // Given
            Long userId = 1L;
            when(userService.updateUser(eq(userId), any(CreateUpdateProfileRequest.class), isNull()))
                    .thenReturn(userResponseDTO);

            // When & Then
            mockMvc.perform(multipart("/api/users/{id}", userId)
                            .param("name", "Updated Name")
                            .param("email", "updated@example.com")
                            .param("bio", "Updated bio")
                            .with(request -> {
                                request.setMethod("PATCH");
                                return request;
                            }))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));

            verify(userService, times(1)).updateUser(eq(userId), any(CreateUpdateProfileRequest.class), isNull());
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // Given
            Long userId = 1L;
            doNothing().when(userService).deleteUser(userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUser(userId);
        }

        @Test
        @DisplayName("Should handle service exception during deletion")
        void shouldHandleServiceExceptionDuringDeletion() throws Exception {
            // Given
            Long userId = 1L;
            doThrow(new RuntimeException("User not found")).when(userService).deleteUser(userId);

            // When & Then
            mockMvc.perform(delete("/api/users/{id}", userId))
                    .andExpect(status().isInternalServerError());

            verify(userService, times(1)).deleteUser(userId);
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should get all users with default pagination")
        void shouldGetAllUsersWithDefaultPagination() throws Exception {
            // Given
            List<UserDTO> userList = Arrays.asList(userDTO);
            Page<UserDTO> userPage = new PageImpl<>(userList, PageRequest.of(0, 10), 1);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            // When & Then
            mockMvc.perform(get("/api/users/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(1L))
                    .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.size").value(10))
                    .andExpect(jsonPath("$.number").value(0));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(userService, times(1)).getAllUsers(pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(0, capturedPageable.getPageNumber());
            assertEquals(10, capturedPageable.getPageSize());
        }

        @Test
        @DisplayName("Should get all users with custom pagination")
        void shouldGetAllUsersWithCustomPagination() throws Exception {
            // Given
            List<UserDTO> userList = Arrays.asList(userDTO);
            Page<UserDTO> userPage = new PageImpl<>(userList, PageRequest.of(1, 5), 1);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            // When & Then
            mockMvc.perform(get("/api/users/all")
                            .param("page", "1")
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.size").value(5))
                    .andExpect(jsonPath("$.number").value(1));

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(userService, times(1)).getAllUsers(pageableCaptor.capture());

            Pageable capturedPageable = pageableCaptor.getValue();
            assertEquals(1, capturedPageable.getPageNumber());
            assertEquals(5, capturedPageable.getPageSize());
        }

        @Test
        @DisplayName("Should handle empty user list")
        void shouldHandleEmptyUserList() throws Exception {
            // Given
            Page<UserDTO> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

            // When & Then
            mockMvc.perform(get("/api/users/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(userService, times(1)).getAllUsers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() throws Exception {
            // Given
            Long userId = 1L;
            when(userService.getUserById(userId)).thenReturn(userDTO);

            // When & Then
            mockMvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.name").value("Test User"))
                    .andExpect(jsonPath("$.bio").value("Test bio"));

            verify(userService, times(1)).getUserById(userId);
        }

        @Test
        @DisplayName("Should handle user not found")
        void shouldHandleUserNotFound() throws Exception {
            // Given
            Long userId = 999L;
            when(userService.getUserById(userId)).thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(get("/api/users/{id}", userId))
                    .andExpect(status().isInternalServerError());

            verify(userService, times(1)).getUserById(userId);
        }
    }

    @Nested
    @DisplayName("Controller Integration Tests")
    class ControllerIntegrationTests {

        @Test
        @DisplayName("Should handle invalid path variables")
        void shouldHandleInvalidPathVariables() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/invalid"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));
        }

        @Test
        @DisplayName("Should handle missing request body")
        void shouldHandleMissingRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(userService, never()).registerUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void shouldHandleInvalidJsonInRequestBody() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.status").value(500));

            verify(userService, never()).registerUser(any(UserDTO.class));
        }

        @Test
        @DisplayName("Should verify controller autowiring")
        void shouldVerifyControllerAutowiring() {
            // Then
            assertNotNull(mockMvc);
            assertNotNull(userService);
            assertNotNull(objectMapper);
        }
    }
}