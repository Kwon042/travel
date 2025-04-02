package com.example.travelProj.domain.like.commentlike;

import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByCommentAndUser(Comment comment, SiteUser user);
    long countByComment(Comment comment);
    void deleteByCommentAndUser(Comment comment, SiteUser user);
}
