package com.example.travelProj.domain.like.boardlike;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.board.ReviewBoardService;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewBoardLikeService {

    private final ReviewBoardLikeRepository likeRepository;
    private final ReviewBoardService reviewBoardService;

    @Transactional
    public void addLike(Long reviewBoardId, SiteUser user) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        if (likeRepository.existsByReviewBoardAndUser(reviewBoard, user)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }
        ReviewBoardLike like = new ReviewBoardLike();
        like.setReviewBoard(reviewBoard);
        like.setUser(user);
        likeRepository.save(like);
    }

    @Transactional
    public void removeLike(Long reviewBoardId, SiteUser user) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        likeRepository.deleteByReviewBoardAndUser(reviewBoard, user);
    }

    public long countLikes(Long reviewBoardId) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        return likeRepository.countByReviewBoard(reviewBoard);
    }

}
