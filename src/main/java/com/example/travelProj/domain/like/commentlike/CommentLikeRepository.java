package com.example.travelProj.domain.like.commentlike;

import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    long countByComment(Comment comment);
    void deleteByCommentAndUser(Comment comment, SiteUser user);

    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    // 모든 좋아요의 수를 합산하는 메서드
    @Query("SELECT COUNT(rbl) FROM CommentLike rbl")
    long countTotalLikes(); // 모든 댓글에 대한 좋아요 수 합산

}
