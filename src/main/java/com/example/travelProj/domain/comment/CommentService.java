package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.travelProj.domain.board.ReviewBoardService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReviewBoardService reviewBoardService;

    @Transactional
    public void addComment(Long reviewBoardId, SiteUser user, String content) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);

        Comment comment = new Comment();
        comment.setReviewBoard(reviewBoard);
        comment.setUser(user);
        comment.setContent(content);

        commentRepository.save(comment);
    }

    // 댓글 목록과 댓글 수를 반환하는 메서드
    public Map<String, Object> getCommentsWithCount(Long reviewBoardId) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        List<Comment> comments = commentRepository.findByReviewBoard(reviewBoard);

        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("commentsCount", comments.size());

        return result;
    }

    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public Comment getCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

}
