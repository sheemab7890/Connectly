package com.sheemab.linkedin.post_service.Controllers;

import com.sheemab.linkedin.post_service.DTOs.PostCreateRequest;
import com.sheemab.linkedin.post_service.DTOs.PostDto;
import com.sheemab.linkedin.post_service.Services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/core")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(path = "/createPost")
    public ResponseEntity<PostDto> createdPost(@RequestBody PostCreateRequest postCreateRequest){
        PostDto createPost = postService.createPost(postCreateRequest);
        return new ResponseEntity<>(createPost , HttpStatus.CREATED);
    }

    @GetMapping(path = "/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable  Long postId){
//        String userId = request.getHeader("X-User-Id");
//        Long userId = UserContextHolder.getCurrentUserId();
        PostDto postDto = postService.getPostById(postId);
            return ResponseEntity.ok(postDto);
    }

   @GetMapping("/users/{userId}/allPosts")
    public ResponseEntity<List<PostDto>> getAllPostOfUser(@PathVariable Long userId){
        List<PostDto> posts = postService.getAllPostOfUser(userId);
        return ResponseEntity.ok(posts);
   }

}
