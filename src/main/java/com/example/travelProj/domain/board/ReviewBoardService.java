package com.example.travelProj.domain.board;

import com.example.travelProj.domain.image.Image;
import com.example.travelProj.domain.image.ImageService;
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
    private final ImageService imageService;

    // 게시글 목록 - 페이징
    @Transactional
    public Page<ReviewBoard> getBoardPage(String regionName, int page, int size) {
        // Pageable 객체 생성 (최신순으로 정렬)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));

        // 지역 이름을 기반으로 작성된 게시글 조회
        if ("전체".equals(regionName)) {
            return reviewBoardRepository.findAll(pageable); // 모든 게시글을 최신순으로 조회
        } else {
            // 주어진 지역 이름으로 지역 객체 조회
            Region region = findByRegionName(regionName)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
            return reviewBoardRepository.findByRegion(region, pageable); // 해당 지역의 게시글을 최신순으로 조회
        }
    }

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
    public void updateReviewBoard(ReviewBoardDTO reviewBoardDTO, MultipartFile file) throws IOException {
        // 게시글 조회
        ReviewBoard reviewBoard = reviewBoardRepository.findById(reviewBoardDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Review Board not found"));

        // Region 객체 가져오기
        Region region = regionRepository.findById(reviewBoardDTO.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

        reviewBoard.setTitle(reviewBoardDTO.getTitle());
        reviewBoard.setContent(reviewBoardDTO.getContent());
        reviewBoard.setUpdatedAt(LocalDateTime.now());
        reviewBoard.setRegion(region);

        // 기존 이미지 리스트 가져오기 (null 방지)
        List<String> existingImageUrls = new ArrayList<>(reviewBoard.getImageUrls());

        // 새로운 이미지가 첨부된 경우 처리
        if (file != null && !file.isEmpty()) {
            String imageUrl = imageService.saveFile(reviewBoardDTO.getId(), "review", file, reviewBoard);
            existingImageUrls.add(imageUrl); // 기존 리스트에 추가
        }

        // 기존 + 새 이미지 URL 갱신
        reviewBoard.setImageUrls(existingImageUrls);
        reviewBoardRepository.save(reviewBoard);
    }

    @Transactional
    public boolean deleteReviewBoard(Long id, SiteUser currentUser) {
        ReviewBoard board = reviewBoardRepository.findById(id)
                .orElse(null);

        // 게시물 작성자와 현재 사용자 ID 비교
        if (board == null || !board.getUser().getId().equals(currentUser.getId())) {
            return false;
        }

        reviewBoardRepository.deleteById(id);
        return true;
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