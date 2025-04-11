package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.board.ReviewBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewBoard_Id(Long reviewBoardId);
    int countByReviewBoard_Id(Long reviewBoardId);

}
