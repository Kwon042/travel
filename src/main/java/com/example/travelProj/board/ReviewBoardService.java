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
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewBoardService {
    @Autowired
    private final ReviewBoardRepository reviewBoardRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    // 게시글 생성 로직 추가
    public ReviewBoard createReviewBoard(ReviewBoardDTO reviewBoardDTO, @AuthenticationPrincipal SiteUser currentUser) {
        ReviewBoard reviewBoard = new ReviewBoard();
        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setRegion(reviewBoardDTO.getRegion());
        reviewBoard.setUser(currentUser);
        reviewBoard.setNickname(reviewBoardDTO.getNickname());
        reviewBoard.setCreatedAt(LocalDateTime.now());
        reviewBoard.setUpdatedAt(LocalDateTime.now());

        // 이미지 URL 리스트 처리
        List<Image> images = new ArrayList<>(); // 이미지 리스트
        for (String imageUrl : reviewBoardDTO.getImageUrls()) {
            Image image = new Image(); // Image 클래스의 인스턴스를 생성
            image.setUrl(imageUrl); // 이미지 URL 설정
            image.setReviewBoard(reviewBoard); // 리뷰 게시판과 관계 설정
            images.add(image);
        }
        reviewBoard.setReview_images(images); // 리뷰 보드에 이미지 설정
        reviewBoard.setMainImageUrlFromImages(); // 메인 이미지 설정

        return reviewBoardRepository.save(reviewBoard);
    }

    // 전체 게시글 조회 로직
    public List<ReviewBoard> getAllBoards() {
        return reviewBoardRepository.findAll();
    }

    // 특정 지역의 게시글 조회 로직
    public List<ReviewBoard> getBoardsByRegion(String region) {
        return reviewBoardRepository.findByRegion(region);
    }

    public ReviewBoard getBoardById(Long id) {
        return reviewBoardRepository.findById(id).orElse(null); // 게시글 조회
    }

}