package com.blogify.BlogApp.service;

import com.blogify.BlogApp.dto.CreateUpdateProfileRequest;
import com.blogify.BlogApp.dto.UserDTO;
import com.blogify.BlogApp.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface UserService {
    UserDTO registerUser(UserDTO userDTO);
    UserResponseDTO updateUser(Long id, CreateUpdateProfileRequest request, MultipartFile image);
    void deleteUser(Long id);
    Page<UserDTO> getAllUsers(Pageable pageable);
    UserDTO getUserById(Long userId);
    UserResponseDTO login(String email, String password);
}
