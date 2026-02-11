package com.sheemab.linkedin.post_service.DTOs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
