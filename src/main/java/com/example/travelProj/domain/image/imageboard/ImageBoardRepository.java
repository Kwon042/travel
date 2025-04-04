package com.example.travelProj.domain.image.imageboard;

import com.example.travelProj.domain.board.ReviewBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageBoardRepository extends JpaRepository<ImageBoard, Long> {
    List<ImageBoard> findByReviewBoard(ReviewBoard reviewBoard);
}
