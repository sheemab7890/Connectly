package com.sheemab.linkedin.post_service.DTOs;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
public class PostCommentsDto {

    private Long id;
    private Long userId;
    private Long postId;
    private String comment;
    private LocalDateTime createdAt;
}
