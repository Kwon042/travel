package com.example.travelProj.domain.board;

import com.example.travelProj.domain.image.Image;
import com.example.travelProj.domain.image.ImageService;
import com.example.travelProj.domain.region.Region;
import com.example.travelProj.domain.region.RegionRepository;
import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReviewBoardService {

    private final ReviewBoardRepository reviewBoardRepository;
    private final RegionRepository regionRepository;
    private final ImageService imageService;

    // 게시글 생성
    @Transactional
    public ReviewBoard createReviewBoard(ReviewBoardDTO reviewBoardDTO,
                                         @AuthenticationPrincipal SiteUser currentUser) {

        ReviewBoard reviewBoard = new ReviewBoard();

        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setUser(currentUser);
        reviewBoard.setNickname(reviewBoardDTO.getNickname());
        reviewBoard.setCreatedAt(LocalDateTime.now());
        reviewBoard.setUpdatedAt(LocalDateTime.now());
        reviewBoard.setRegion(reviewBoardDTO.getRegion());

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

        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setUpdatedAt(LocalDateTime.now());
        reviewBoard.setRegion(reviewBoardDTO.getRegion());

        // 기존 이미지 리스트 가져오기 (null 방지)
        List<String> existingImageUrls = new ArrayList<>(reviewBoard.getImageUrls());

        // 새로운 이미지가 첨부된 경우 처리
        if (file != null && !file.isEmpty()) {
            String imageUrl = imageService.saveFile(reviewBoardId, "review", file, reviewBoard);
            existingImageUrls.add(imageUrl);  // 기존 리스트에 추가
        }

        // 기존 + 새 이미지 URL 갱신
        reviewBoard.setImageUrls(existingImageUrls);

        reviewBoardRepository.save(reviewBoard);
    }

    // 전체 게시글 조회 로직
    public List<ReviewBoard> getAllBoards() {
        List<ReviewBoard> boards = reviewBoardRepository.findAll();
        System.out.println("전체 게시글 조회: " + boards.size());
        return boards;
    }

    // 특정 지역의 게시글 조회
    public List<ReviewBoard> getBoardsByRegion(Region region) {
        return reviewBoardRepository.findByRegion_RegionName(region.getRegionName());
    }

    // 주어진 지역 이름으로 지역 정보를 검색하는 메서드 - 각 지역 게시판으로 이동할 때
    public Optional<Region> findByRegionName(String regionName) {
        return regionRepository.findByRegionName(regionName);
    }

    // 특정 ID에 해당하는 게시글을 조회
    public ReviewBoard getBoardById(Long id) {
        return reviewBoardRepository.findById(id).orElse(null);
    }

}