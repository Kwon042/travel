package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.travelProj.domain.board.ReviewBoardService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    public List<Comment> getComments(Long reviewBoardId) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        return commentRepository.findByReviewBoard(reviewBoard);
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
