package com.sheemab.linkedin.notification_service.Consumer;

import com.sheemab.linkedin.notification_service.Dto.PersonDto;
import com.sheemab.linkedin.notification_service.Service.SendNotification;
import com.sheemab.linkedin.notification_service.client.ConnectionsClient;
import com.sheemab.linkedin.post_service.Event.PostCommentEvent;
import com.sheemab.linkedin.post_service.Event.PostCreatedEvent;
import com.sheemab.linkedin.post_service.Event.PostLikedEvent;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostServiceConsumer {
    private final ConnectionsClient connectionsClient;
    private final SendNotification notificationService;

         @KafkaListener(topics = "post-created-topic")
        public void handlePostCreated(PostCreatedEvent postCreatedEvent) {
            log.info("Received PostCreatedEvent: {}", postCreatedEvent);

            try {
                // Fetch connections
                List<PersonDto> connections = connectionsClient.getFirstDegreeConnections(postCreatedEvent.getCreatorId());
                log.info("Fetched {} connections for userId {}", connections.size(), postCreatedEvent.getCreatorId());

                // Send notifications to connections
                for (PersonDto connection : connections) {
                    notificationService.sendNotification(connection.getUserId(),
                            "Your connection " + postCreatedEvent.getCreatorId() + " has created a post. Check it out!");
                }
            } catch (FeignException e) {
                log.error("Error while calling connections service: {}", e.getMessage(), e);
            } catch (Exception e) {
                log.error("Unexpected error in handlePostCreated: {}", e.getMessage(), e);
            }
        }


    @KafkaListener(topics = "post-liked-topic")
    public void handlePostLike(PostLikedEvent postLikedEvent){
        log.info("Sending notifications: handlePostLike: {}",postLikedEvent);
        String message = String.format("Your post, %d has been liked by %d",
                postLikedEvent.getPostId(),
                postLikedEvent.getLikedByUserId());
        notificationService.sendNotification(postLikedEvent.getCreatorId(), message);
    }

    @KafkaListener(topics = "post-comment-topic")
    public void handlePostComment(PostCommentEvent postCommentEvent){
        log.info("Sending notification: handlePostComment: {}",postCommentEvent);
        String message = String.format("%d, comments %s, on your post %d",
                postCommentEvent.getUserId(),
                postCommentEvent.getComment(),
                postCommentEvent.getPostId());

        notificationService.sendNotification(postCommentEvent.getCreatorId(), message);
    }


}
