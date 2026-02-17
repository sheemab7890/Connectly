package com.sheemab.linkedin.user_service.Controllers;


import com.sheemab.linkedin.user_service.DTO.LogInResponseDto;
import com.sheemab.linkedin.user_service.DTO.LoginRequestDto;
import com.sheemab.linkedin.user_service.DTO.SignUpRequestDto;
import com.sheemab.linkedin.user_service.DTO.UserDto;
import com.sheemab.linkedin.user_service.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
     private final UserService userService;

     @PostMapping("/signUp")
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto){
         UserDto signUp = userService.signUp(signUpRequestDto);
         return new ResponseEntity<>(signUp, HttpStatus.CREATED);
     }

     @PostMapping("/logIn")
    public ResponseEntity<LogInResponseDto> logIn(@RequestBody LoginRequestDto loginRequestDto){
         LogInResponseDto token  = userService.logIn(loginRequestDto);
         return ResponseEntity.ok(token);
     }
}

/*

$body = @{
    name = "User2"
    email = "user2@test.com"
    password = "Password123"
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri "http://localhost:8090/users/auth/signUp" -ContentType "application/json" -Body $body

 */
