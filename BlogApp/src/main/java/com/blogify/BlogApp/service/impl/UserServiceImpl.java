package com.blogify.BlogApp.service.impl;

import com.blogify.BlogApp.dto.CreateUpdateProfileRequest;
import com.blogify.BlogApp.dto.UserDTO;
import com.blogify.BlogApp.dto.UserResponseDTO;
import com.blogify.BlogApp.entity.User;
import com.blogify.BlogApp.exception.BadRequestException;
import com.blogify.BlogApp.exception.ResourceNotFoundException;
import com.blogify.BlogApp.exception.UnauthorizedException;
import com.blogify.BlogApp.repository.UserRepository;
import com.blogify.BlogApp.service.UserService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.cloudinary = cloudinary;
    }

    @Override
    public UserResponseDTO updateUser(Long id, CreateUpdateProfileRequest request, MultipartFile image){
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found."));
        String imgUrl = user.getProfileImage();
        if(image != null && !image.isEmpty()){
            try{
                if(imgUrl !=null && !imgUrl.isEmpty()){
                    String publicId = imgUrl.substring(imgUrl.lastIndexOf("/") + 1, imgUrl.lastIndexOf("."));
                    cloudinary.uploader().destroy("blog_users/" + publicId, ObjectUtils.emptyMap());
                }
                Map uploadResult = cloudinary.uploader().upload(
                        image.getBytes(),
                        ObjectUtils.asMap("folder","blog_users")
                );
                imgUrl = (String) uploadResult.get("secure_url");
                user.setProfileImage(imgUrl);
            }catch (IOException e){
                throw new RuntimeException("Image upload failed",e);
            }
        }
        if(request.getName() !=null){
            user.setName(request.getName());
        }
        if(request.getBio() !=null){
            user.setBio(request.getBio());
        }
        if (request.getEmail() != null){
            user.setEmail(request.getEmail());
        }

        User savedUser = userRepository.save(user);
        UserResponseDTO response = new UserResponseDTO();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setName(savedUser.getName());
        response.setBio(savedUser.getBio());
        response.setProfileImage(savedUser.getProfileImage());
        response.setRoles(savedUser.getRoles());
        return response;
    }

    @Override
    public UserDTO registerUser(UserDTO userDTO){
        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new BadRequestException("Email already exists!");
        }
        User user = modelMapper.map(userDTO,User.class);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        if(user.getRoles() == null || user.getRoles().isEmpty()){
            user.setRoles(new HashSet<>());
            user.getRoles().add("ROLE_USER");
        }
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser,UserDTO.class);
    }

    @Override
    public void deleteUser(Long id){
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found."));
        userRepository.delete(user);
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable){
        return userRepository.findAll(pageable).map(user -> modelMapper.map(user,UserDTO.class));
    }

    @Override
    public UserDTO getUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found."));
        return modelMapper.map(user,UserDTO.class);
    }

    @Override
    public UserResponseDTO login(String email, String password){
        User user = userRepository.findByEmail(email).orElseThrow(()->new BadRequestException("Invalid Credentials."));
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new BadRequestException("Invalid Credentials.");
        }
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setBio(user.getBio());
        response.setProfileImage(user.getProfileImage());
        response.setRoles(user.getRoles());
        return response;
    }
}
