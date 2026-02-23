package com.sheemab.linkedin.user_service.Controllers;


import com.sheemab.linkedin.user_service.DTO.LogInResponseDto;
import com.sheemab.linkedin.user_service.DTO.LoginRequestDto;
import com.sheemab.linkedin.user_service.DTO.SignUpRequestDto;
import com.sheemab.linkedin.user_service.DTO.UserDto;
import com.sheemab.linkedin.user_service.Services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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


     // Now browser will store JWT automatically.
    @PostMapping("/login")
    public ResponseEntity<LogInResponseDto> login(
            @RequestBody LoginRequestDto request,
            HttpServletResponse response) {

        LogInResponseDto loginResponse = userService.logIn(request);

        String token = loginResponse.getAccessToken();

        Cookie cookie = new Cookie("access_token", token); // How to set anything in cookie
        cookie.setHttpOnly(true);
        cookie.setSecure(false); //  false because you are using HTTP
        cookie.setPath("/");
        cookie.setMaxAge(60 * 10); // 10 minutes

        response.addCookie(cookie);

        return ResponseEntity.ok(loginResponse);
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
