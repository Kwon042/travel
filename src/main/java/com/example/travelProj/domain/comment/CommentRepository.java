package com.example.travelProj.domain.comment;

import com.example.travelProj.domain.board.ReviewBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByReviewBoard_Id(Long reviewBoardId);
    // 최상위 댓글만 가져와서 대댓글 포함 개수 계산
    List<Comment> findByReviewBoard_IdAndParentIsNull(Long reviewBoardId);

}
