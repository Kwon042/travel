package com.example.travelProj.domain.like.commentlike;

import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.comment.CommentService;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentService commentService;

    @Transactional
    public boolean toggleLike(Long commentId, SiteUser user) {
        // 해당 댓글에 대한 좋아요가 이미 존재하는지 확인
        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, user.getId());

        if (existingLike.isPresent()) {
            // 이미 좋아요를 눌렀으면 삭제 (좋아요 취소)
            commentLikeRepository.delete(existingLike.get());
            return false;  // 좋아요 취소 상태
        } else {
            // 좋아요를 누르지 않았으면 추가
            Comment comment = commentService.getCommentById(commentId);
            CommentLike newLike = new CommentLike();
            newLike.setComment(comment);
            newLike.setUser(user);
            commentLikeRepository.save(newLike);
            return true;  // 좋아요 상태
        }
    }

    @Transactional
    public void removeLike(Long commentId, SiteUser user) {
        Comment comment = commentService.getCommentById(commentId);
        commentLikeRepository.deleteByCommentAndUser(comment, user);
    }

    public boolean isLikedByUser(Long commentId, SiteUser user) {
        return commentLikeRepository.findByCommentIdAndUserId(commentId, user.getId()).isPresent();
    }

    public long countLikes(Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        return commentLikeRepository.countByComment(comment);
    }

    public long countTotalLikes() { return commentLikeRepository.countTotalLikes(); }

}
