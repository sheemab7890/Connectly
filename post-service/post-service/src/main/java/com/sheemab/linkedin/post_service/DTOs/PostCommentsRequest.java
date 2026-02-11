package com.sheemab.linkedin.post_service.DTOs;

import lombok.Data;

@Data
public class PostCommentsRequest {

    private Long id;
    private Long userId;
    private String comment;
}
