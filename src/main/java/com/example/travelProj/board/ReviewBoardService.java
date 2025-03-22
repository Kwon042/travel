package com.example.travelProj.board;

import com.example.travelProj.Image;
import com.example.travelProj.user.SiteUser;
import com.example.travelProj.user.UserRepository;
import com.example.travelProj.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewBoardService {
    @Autowired
    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // 게시글 생성 로직 추가
    public ReviewBoard createReviewBoard(String title, String content, String region,
                                         @AuthenticationPrincipal SiteUser currentUser) {
        ReviewBoard reviewBoard = new ReviewBoard();
        reviewBoard.setTitle(title);
        reviewBoard.setContent(content);
        reviewBoard.setRegion(region);
        reviewBoard.setUser(currentUser);
        reviewBoard.setCreatedAt(LocalDateTime.now());
        return reviewBoardRepository.save(reviewBoard);
    }

    // 전체 게시글 조회 로직
    public List<ReviewBoard> getAllBoards() {
        return reviewBoardRepository.findAll(); // 전체 게시글을 가져오는 메서드
    }

    // 특정 지역의 게시글 조회 로직
    public List<ReviewBoard> getBoardsByRegion(String region) {
        return reviewBoardRepository.findByRegion(region); // 지역에 따라 게시글 가져오기
    }

}