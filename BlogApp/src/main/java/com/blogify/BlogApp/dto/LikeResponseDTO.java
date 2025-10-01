package com.blogify.BlogApp.dto;

public class LikeResponseDTO {
    private Long postId;
    private Long likeCount;
    private boolean likedByUser;

    public LikeResponseDTO(Long postId, Long likeCount, boolean likedByUser) {
        this.postId = postId;
        this.likeCount = likeCount;
        this.likedByUser = likedByUser;
    }

    public Long getPostId() { return postId; }
    public Long getLikeCount() { return likeCount; }
    public boolean isLikedByUser() { return likedByUser; }
}