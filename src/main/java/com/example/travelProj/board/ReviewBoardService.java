package com.example.travelProj.board;

import com.example.travelProj.Image;
import com.example.travelProj.ImageService;
import com.example.travelProj.Region;
import com.example.travelProj.RegionRepository;
import com.example.travelProj.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ReviewBoardService {

    private final ReviewBoardRepository reviewBoardRepository;
    private final RegionRepository regionRepository;
    private final ImageService imageService;

    // 게시글 생성
    @Transactional
    public ReviewBoard createReviewBoard(ReviewBoardDTO reviewBoardDTO, @AuthenticationPrincipal SiteUser currentUser) {
        ReviewBoard reviewBoard = new ReviewBoard();
        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setUser(currentUser);
        reviewBoard.setNickname(reviewBoardDTO.getNickname());
        reviewBoard.setCreatedAt(LocalDateTime.now());
        reviewBoard.setUpdatedAt(LocalDateTime.now());

        // regionName을 Region 객체로 변환
        if (reviewBoardDTO.getRegionName() != null) {
            Region region = regionRepository.findByRegionName(reviewBoardDTO.getRegionName())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
            reviewBoard.setRegion(region);
        }

        // 이미지 URL 리스트 처리
        List<Image> images = new ArrayList<>();
        for (String imageUrl : reviewBoardDTO.getImageUrls()) {
            Image image = new Image();
            image.setUrl(imageUrl);
            image.setReviewBoard(reviewBoard);
            images.add(image);
        }
        reviewBoard.setReview_images(images); // 리뷰 보드에 이미지 설정
        reviewBoard.setMainImageUrlFromImages(); // 메인 이미지 설정

        return reviewBoardRepository.save(reviewBoard);
    }

    // 게시글 수정
    @Transactional
    public void updateReviewBoard(Long reviewBoardId, ReviewBoardDTO reviewBoardDTO, MultipartFile file) throws IOException {
        // 게시글 조회
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardId)
                .orElseThrow(() -> new IllegalArgumentException("Review Board not found"));

        // 게시글 제목, 내용, 업데이트 시간 수정
        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setUpdatedAt(LocalDateTime.now());

        // 지역 업데이트
        if (reviewBoardDTO.getRegionName() != null) {
            Region region = regionRepository.findByRegionName(reviewBoardDTO.getRegionName())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
            reviewBoard.setRegion(region);
        }

        // 이미지가 첨부되었으면 처리
        if (file != null && !file.isEmpty()) {
            String imageUrl = imageService.saveFile(reviewBoardId, "review", file, reviewBoard);  // 이미지 저장
            reviewBoardDTO.getImageUrls().add(imageUrl); // URL 추가
        }

        // 기존 이미지 URL 갱신
        reviewBoard.setImageUrls(reviewBoardDTO.getImageUrls()); // 이미지 URL 리스트 갱신
        reviewBoardRepository.save(reviewBoard); // DB에 저장
    }

    // 전체 게시글 조회 로직
    public List<ReviewBoard> getAllBoards() {
        List<ReviewBoard> boards = reviewBoardRepository.findAll();
        System.out.println("전체 게시글 조회: " + boards.size());  // 디버깅 로그
        return boards;
    }

    // 특정 지역의 게시글 조회 로직
    public List<ReviewBoard> getBoardsByRegion(Region region) {
        return reviewBoardRepository.findByRegion_RegionName(region.getRegionName());
    }

    public List<ReviewBoard> findByRegionName(Region region) {
        return reviewBoardRepository.findByRegion_RegionName(region.getRegionName());
    }

    public ReviewBoard getBoardById(Long id) {
        return reviewBoardRepository.findById(id).orElse(null); // 게시글 조회
    }

}