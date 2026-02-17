package com.sheemab.linkedin.post_service.Services;

import com.sheemab.linkedin.post_service.Entities.Post;
import com.sheemab.linkedin.post_service.Entities.PostLikes;
import com.sheemab.linkedin.post_service.Exception.BadRequestException;
import com.sheemab.linkedin.post_service.Exception.ResourceNotFoundException;
import com.sheemab.linkedin.post_service.Repositories.LikeRepository;
import com.sheemab.linkedin.post_service.Repositories.PostRepository;
import com.sheemab.linkedin.post_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.events.PostLikedEvent;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository postLikeRepository;
    private final ModelMapper modelMapper;
    private final PostRepository postRepository;
    private final KafkaTemplate<String , Object> kafkaTemplate;

    public void likePost(Long postId)   {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Attempting to like post with id: {}" ,postId);

        Post post = postRepository.findById(postId).orElseThrow(
                ()->  new ResourceNotFoundException("Post not found with id:"+postId)
        );

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(postId, userId);
        if(alreadyLiked) throw new BadRequestException("Cannot like the same post again");

        PostLikes postLikes = new PostLikes();
        postLikes.setPostId(postId);
        postLikes.setUserId(userId);

        postLikeRepository.save(postLikes);
        log.info("Post with id: {} Like successfully ",postId);

        PostLikedEvent postLikedEvent = PostLikedEvent.builder()
                .postId(postId)
                .likedByUserId(userId)
                .creatorId(post.getUserId())
                .build();

        kafkaTemplate.send("post-liked-topic", String.valueOf(postId),postLikedEvent);
    }

    public void unLikePost(Long postId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Attempting to unlike post with id: {}" ,postId);
        boolean exist = postRepository.existsById(postId);
        if(!exist) throw new ResourceNotFoundException("Post not found with id:"+postId);

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(userId, postId);
        if(!alreadyLiked) throw new BadRequestException("Cannot unLike the post which is not Liked");

        postLikeRepository.deleteByUserIdAndPostId(userId,postId);
        log.info("Post with id: {} unLike successfully ",postId);

    }
}
