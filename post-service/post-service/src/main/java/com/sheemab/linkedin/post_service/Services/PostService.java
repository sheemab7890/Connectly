package com.sheemab.linkedin.post_service.Services;

import com.sheemab.linkedin.post_service.DTOs.PersonDto;
import com.sheemab.linkedin.post_service.DTOs.PostCreateRequest;
import com.sheemab.linkedin.post_service.DTOs.PostDto;
import com.sheemab.linkedin.post_service.Entities.Post;
import com.sheemab.linkedin.post_service.Exception.ResourceNotFoundException;
import com.sheemab.linkedin.post_service.Repositories.PostRepository;
import com.sheemab.linkedin.post_service.client.ConnectionsClient;
import com.sheemab.linkedin.post_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.PostCreatedEvent;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsClient connectionsClient;
    private final KafkaTemplate<String , Object> kafkaTemplate;

    public PostDto createPost(PostCreateRequest postCreateRequest) {
        Long userId = SecurityUtils.getCurrentUserId();
      Post post = modelMapper.map(postCreateRequest , Post.class);
      post.setUserId(userId);

      Post savedPost = postRepository.save(post);

      PostCreatedEvent postCreatedEvent = PostCreatedEvent.builder()
                .postId(savedPost.getId())
                .creatorId(userId)
                .content(savedPost.getContent())
                .build();

      kafkaTemplate.send("post-created-topic",postCreatedEvent);
      return modelMapper.map(savedPost , PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.debug("Retrieving post with ID:{}",postId);

        Long userId = SecurityUtils.getCurrentUserId();

       List<PersonDto> firstConnection = connectionsClient.getFirstDegreeConnections();

        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new ResourceNotFoundException("Post not found with id:" +postId)
        );
        return modelMapper.map(post , PostDto.class);
    }

    public List<PostDto> getAllPostOfUser(Long userId) {

        List<Post> posts = postRepository.findByUserId(userId);
        return posts
                .stream()
                .map(element -> modelMapper.map(element , PostDto.class))
                .collect(Collectors.toList());
    }
}
