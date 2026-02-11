package com.sheemab.linkedin.post_service.Controllers;

import com.sheemab.linkedin.post_service.Advices.ApiResponse;
import com.sheemab.linkedin.post_service.DTOs.PostCommentsDto;
import com.sheemab.linkedin.post_service.DTOs.PostCommentsRequest;
import com.sheemab.linkedin.post_service.Services.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/comments")
public class CommentsController {
    private final CommentsService commentsService;

    @PostMapping("/{postId}")
    public ResponseEntity<PostCommentsDto> commentOnPost(@RequestBody PostCommentsRequest postCommentsRequest,
                                                        @PathVariable Long postId){
        PostCommentsDto postCommentsDto = commentsService.commentOnPost(postCommentsRequest, postId);
        return ResponseEntity.ok(postCommentsDto);
    }

    @GetMapping("/getAllComments/{postId}")
    public ResponseEntity<List<PostCommentsRequest>> getAllCommentsOfPost(@PathVariable Long postId){
        List<PostCommentsRequest> comments = commentsService.getAllComments(postId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{postId}/delete/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Long postId,
                                                     @PathVariable Long commentId){
        commentsService.deleteComments(postId,commentId);

        // Constructing the ApiResponse object
        ApiResponse response = new ApiResponse("Comments deleted successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
}
