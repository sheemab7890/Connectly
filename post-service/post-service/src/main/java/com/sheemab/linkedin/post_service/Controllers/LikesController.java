package com.sheemab.linkedin.post_service.Controllers;

import com.sheemab.linkedin.post_service.Repositories.LikeRepository;
import com.sheemab.linkedin.post_service.Services.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/likes")
@RequiredArgsConstructor
public class LikesController {

    private final LikeService likeService;

    @PostMapping("/like/{postId}")
    public ResponseEntity<Void> likePost(@PathVariable Long postId){
        likeService.likePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/dislike/{postId}")
    public ResponseEntity<Void> unLikePost(@PathVariable Long postId){
        likeService.unLikePost(postId);
        return ResponseEntity.noContent().build();
    }
}
