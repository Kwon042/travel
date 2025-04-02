package com.example.travelProj.domain.like.boardlike;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewBoardLikeRepository extends JpaRepository<ReviewBoardLike, Long> {
    boolean existsByReviewBoardAndUser(ReviewBoard reviewBoard, SiteUser user);
    long countByReviewBoard(ReviewBoard reviewBoard);
    void deleteByReviewBoardAndUser(ReviewBoard reviewBoard, SiteUser user);
}
