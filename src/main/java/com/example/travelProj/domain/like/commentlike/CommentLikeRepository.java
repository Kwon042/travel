package com.example.travelProj.domain.like.commentlike;

import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    long countByComment(Comment comment);
    void deleteByCommentAndUser(Comment comment, SiteUser user);

    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

}
