package com.example.travelProj.domain.like.boardlike;

import com.example.travelProj.domain.board.ReviewBoard;
import com.example.travelProj.domain.board.ReviewBoardService;
import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewBoardLikeService {

    private final ReviewBoardLikeRepository reviewBoardLikeRepository;
    private final ReviewBoardService reviewBoardService;

    @Transactional
    public void addLike(Long reviewBoardId, SiteUser user) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        if (reviewBoardLikeRepository.existsByReviewBoardAndUser(reviewBoard, user)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }
        ReviewBoardLike like = new ReviewBoardLike();
        like.setReviewBoard(reviewBoard);
        like.setUser(user);
        reviewBoardLikeRepository.save(like);
    }

    @Transactional
    public void removeLike(Long reviewBoardId, SiteUser user) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        reviewBoardLikeRepository.deleteByReviewBoardAndUser(reviewBoard, user);
    }

    @Transactional
    public void removeAllLikes(Long reviewBoardId) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        reviewBoardLikeRepository.deleteByReviewBoard(reviewBoard);  // 해당 게시글과 관련된 모든 좋아요 삭제
    }

    public long countTotalLikes() { return reviewBoardLikeRepository.countTotalLikes(); }

    public long countLikes(Long reviewBoardId) {
        return reviewBoardLikeRepository.countByReviewBoardId(reviewBoardId); // 리뷰 게시판에 대한 좋아요 개수
    }

    // 좋아요를 누른 사용자 목록 조회
    public List<UserResponseDTO> getLikeUsers(Long reviewBoardId) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        List<ReviewBoardLike> likes = reviewBoardLikeRepository.findByReviewBoard(reviewBoard);
        return likes.stream()
                .map(like -> new UserResponseDTO(
                        like.getUser().getNickname(),
                        like.getUser().getProfileImageUrl(),
                        like.getUser().getEmail()
                ))
                .collect(Collectors.toList());
    }

    // 사용자가 해당 게시글 좋아요를 눌렀는지 확인
    public boolean hasUserLiked(Long reviewBoardId, SiteUser user) {
        ReviewBoard reviewBoard = reviewBoardService.getBoardById(reviewBoardId);
        return reviewBoardLikeRepository.existsByReviewBoardAndUser(reviewBoard, user);
    }

}
