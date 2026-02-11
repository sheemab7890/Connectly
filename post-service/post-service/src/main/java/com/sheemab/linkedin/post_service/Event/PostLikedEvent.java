package com.sheemab.linkedin.post_service.Event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostLikedEvent {
       Long postId;
       Long creatorId;
       Long likedByUserId;
}
