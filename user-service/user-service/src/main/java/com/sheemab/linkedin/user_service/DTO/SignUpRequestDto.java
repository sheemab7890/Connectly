package com.sheemab.linkedin.user_service.DTO;

import lombok.Data;

@Data
public class SignUpRequestDto {
    private String name;
    private String email;
    private String password;
}
