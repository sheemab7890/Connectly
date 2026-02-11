package com.sheemab.linkedin.user_service.DTO;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String email;
    private String password;
}
