package com.sheemab.linkedin.post_service.Services;

import com.sheemab.linkedin.post_service.DTOs.PostCommentsDto;
import com.sheemab.linkedin.post_service.DTOs.PostCommentsRequest;
import com.sheemab.linkedin.post_service.Entities.Post;
import com.sheemab.linkedin.post_service.Entities.PostComments;
import com.sheemab.linkedin.post_service.Exception.BadRequestException;
import com.sheemab.linkedin.post_service.Exception.ResourceNotFoundException;
import com.sheemab.linkedin.post_service.Repositories.CommentsRepository;
import com.sheemab.linkedin.post_service.Repositories.PostRepository;
import com.sheemab.linkedin.post_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.example.events.PostCommentEvent;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final KafkaTemplate<String , Object> kafkaTemplate;

    public PostCommentsDto commentOnPost(PostCommentsRequest postCommentsRequest, Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Post post = postRepository.findById(postId).orElseThrow(
                ()->  new ResourceNotFoundException("Post not found with id:"+postId)
        );

        PostComments postComments = new PostComments();
        postComments.setPostId(postId);
        postComments.setUserId(userId);
        postComments.setComment(postCommentsRequest.getComment());
        PostComments savedComments = commentsRepository.save(postComments);

        PostCommentEvent postCommentEvent = PostCommentEvent.builder()
                .postId(postId)
                .userId(userId)
                .creatorId(post.getUserId())
                .comment(savedComments.getComment())
                .build();

        kafkaTemplate.send("post-comment-topic", String.valueOf(postId),postCommentEvent);

        return modelMapper.map(savedComments , PostCommentsDto.class);

    }

    public List<PostCommentsRequest> getAllComments(Long postId) {
        boolean exist = postRepository.existsById(postId);
        if(!exist) throw new ResourceNotFoundException("Post not found with id:"+postId);

        List<PostComments> commentResponses = commentsRepository.findByPostId(postId);
      return  commentResponses
                .stream()
                .map(element -> modelMapper.map(element, PostCommentsRequest.class))
                .collect(Collectors.toList());
    }

    public void deleteComments(Long postId, Long commentId) {
        boolean postExist = postRepository.existsById(postId);
        if(!postExist) throw new ResourceNotFoundException("Post not found with id:"+postId);

        Optional<PostComments> comments = commentsRepository.findById(commentId);

        if(comments.isEmpty()) throw new BadRequestException("Comments not found with id:"+commentId);

        if(!comments.get().getPostId().equals(postId)) throw new BadRequestException("Comment does not belong to the post with id:" + postId);
//        boolean isExist = commentsRepository.existsById(commentId);
//        if(!isExist) throw new BadRequestException("Comments already deleted");

        commentsRepository.deleteById(commentId);
    }
}
