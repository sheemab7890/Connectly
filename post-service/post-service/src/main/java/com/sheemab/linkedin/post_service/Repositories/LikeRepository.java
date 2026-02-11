package com.sheemab.linkedin.post_service.Repositories;

import com.sheemab.linkedin.post_service.Entities.Post;
import com.sheemab.linkedin.post_service.Entities.PostLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LikeRepository extends JpaRepository<PostLikes, Long> {

  boolean existsByUserIdAndPostId(Long userId , Long postId);

  @Transactional
  void deleteByUserIdAndPostId(Long userId, Long postId);

}
