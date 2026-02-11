package com.sheemab.linkedin.post_service.Event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class PostLikedEvent {
       Long postId;
       Long creatorId;
       Long likedByUserId;


}
