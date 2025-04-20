package com.example.travelProj.domain.like.boardlike;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewBoardLikeRepository extends JpaRepository<ReviewBoardLike, Long> {
    boolean existsByReviewBoardAndUser(ReviewBoard reviewBoard, SiteUser user);
    long countByReviewBoardId(Long reviewBoard);
    void deleteByReviewBoardAndUser(ReviewBoard reviewBoard, SiteUser user);
    // 특정 리뷰 게시판에 대한 좋아요 리스트 조회
    List<ReviewBoardLike> findByReviewBoard(ReviewBoard reviewBoard);
}
