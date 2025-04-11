package com.example.travelProj.domain.board;

import com.example.travelProj.domain.image.imageboard.ImageBoard;
import com.example.travelProj.domain.image.imageboard.ImageBoardRepository;
import com.example.travelProj.domain.image.imageboard.ImageBoardService;
import com.example.travelProj.domain.region.Region;
import com.example.travelProj.domain.region.RegionRepository;
import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ImageBoardService imageBoardService;

    // 게시글 목록 - 페이징
    @Transactional
    public Page<ReviewBoard> getBoardPage(String regionName, int page, int size) {
        // Pageable 객체 생성 (최신순으로 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        Page<ReviewBoard> boardPage;

        // 지역 이름을 기반으로 작성된 게시글 조회
        if ("전체".equals(regionName)) {
            boardPage = reviewBoardRepository.findAll(pageable); // 모든 게시글을 최신순으로 조회
        } else {
            // 주어진 지역 이름으로 지역 객체 조회
            Region region = findByRegionName(regionName)
                    .orElseThrow(() -> new IllegalArgumentException("This region does not exist."));
            boardPage = reviewBoardRepository.findByRegion(region, pageable); // 해당 지역의 게시글을 최신순으로 조회
        }
        // 👉 각 게시글에 대해 대표 이미지 설정
        boardPage.forEach(ReviewBoard::setMainImageUrlFromImages);

        return boardPage;
    }

    // 게시글 생성
    @Transactional
    public ReviewBoard createReviewBoard(ReviewBoardDTO reviewBoardDTO,
                                         @AuthenticationPrincipal SiteUser currentUser) {

        ReviewBoard reviewBoard = new ReviewBoard();

        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setUser(currentUser);
        String nickname = currentUser.getNickname();
        reviewBoard.setCreatedAt(LocalDateTime.now());
        reviewBoard.setUpdatedAt(LocalDateTime.now());
        reviewBoard.setRegion(reviewBoardDTO.getRegion());

        // 이미지 저장
        List<ImageBoard> images = saveImagesIfPresent(reviewBoardDTO.getImageUrls(), reviewBoard);
        reviewBoard.setReview_images(images); // 리뷰 보드에 이미지 설정
        reviewBoard.setMainImageUrlFromImages(); // 메인 이미지 설정

        return reviewBoardRepository.save(reviewBoard);
    }

    // 이미지 저장 및 처리
    private List<ImageBoard> saveImagesIfPresent(List<String> imageUrls, ReviewBoard reviewBoard) {
        List<ImageBoard> images = new ArrayList<>();

        // URL 리스트를 기반으로 이미지 객체 생성
        for (String imageUrl : imageUrls) {
            ImageBoard imageBoard = new ImageBoard();
            imageBoard.setUrl(imageUrl);
            imageBoard.setReviewBoard(reviewBoard);
            images.add(imageBoard);
        }
        return images;
    }

    // 게시글 수정
    @Transactional
    public void updateReviewBoard(ReviewBoardDTO reviewBoardDTO,
                                  List<MultipartFile> files,
                                  List<Long> imageIdsToDelete) throws IOException {
        // 게시글 조회
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("ReviewBoard not found"));
        Region region = regionRepository.findById(reviewBoardDTO.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

        // 내용 업데이트
        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setRegion(region);
        reviewBoard.setUpdatedAt(LocalDateTime.now());

        // 이미지 삭제 처리
        if (imageIdsToDelete != null && !imageIdsToDelete.isEmpty()) {
            for (Long imageId : imageIdsToDelete) {
                imageBoardService.deleteImage(imageId); // 이미지 삭제
            }
        }

        // 새 이미지 저장 (기존 이미지를 유지)
        if (files != null && !files.isEmpty()) {
            List<ImageBoard> newImages = imageBoardService.saveImages(files, reviewBoard.getId(), reviewBoard.getReview_images());
            reviewBoard.setReview_images(newImages); // 업데이트된 이미지 리스트 설정
        }
        reviewBoardRepository.save(reviewBoard); // 최종 업데이트
    }

    @Transactional
    public void deleteReviewBoard(Long id) {
        reviewBoardRepository.deleteById(id);
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