package com.blogify.BlogApp.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateUpdateProfileRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String email;
    private String bio;

    public CreateUpdateProfileRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
