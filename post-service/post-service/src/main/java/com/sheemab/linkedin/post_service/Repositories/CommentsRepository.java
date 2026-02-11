package com.sheemab.linkedin.post_service.Repositories;

import com.sheemab.linkedin.post_service.Entities.PostComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<PostComments , Long> {
    List<PostComments> findByPostId(Long postId);

}
