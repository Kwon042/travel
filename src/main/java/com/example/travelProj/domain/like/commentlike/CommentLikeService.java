package com.example.travelProj.domain.like.commentlike;

import com.example.travelProj.domain.comment.Comment;
import com.example.travelProj.domain.comment.CommentService;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository likeRepository;
    private final CommentService commentService;

    @Transactional
    public void addLike(Long commentId, SiteUser user) {
        Comment comment = commentService.getCommentById(commentId);
        if (likeRepository.existsByCommentAndUser(comment, user)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }
        CommentLike like = new CommentLike();
        like.setComment(comment);
        like.setUser(user);
        likeRepository.save(like);
    }

    @Transactional
    public void removeLike(Long commentId, SiteUser user) {
        Comment comment = commentService.getCommentById(commentId);
        likeRepository.deleteByCommentAndUser(comment, user);
    }

    public long countLikes(Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        return likeRepository.countByComment(comment);
    }
}
