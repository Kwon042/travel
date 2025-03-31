package com.example.travelProj.domain.like;

import com.example.travelProj.domain.attraction.Attraction;
import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByReviewBoard(ReviewBoard reviewBoard);
    long countByComment(Comment comment);
    long countByAttraction(Attraction attraction);
}
