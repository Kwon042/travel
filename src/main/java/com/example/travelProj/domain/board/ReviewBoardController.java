package com.example.travelProj.domain.board;

import com.example.travelProj.domain.image.imageboard.ImageBoard;
import com.example.travelProj.domain.image.imageboard.ImageBoardService;
import com.example.travelProj.domain.like.boardlike.ReviewBoardLikeService;
import com.example.travelProj.domain.region.Region;
import com.example.travelProj.domain.region.RegionRepository;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class ReviewBoardController {

    private final CsrfTokenRepository csrfTokenRepository;
    private final RegionRepository regionRepository;
    private final ReviewBoardService reviewBoardService;
    private final ImageBoardService imageBoardService;
    private final ReviewBoardLikeService reviewBoardLikeService;
    private static final Logger logger = LoggerFactory.getLogger(ReviewBoardController.class);

    // 리뷰 게시판 목록
    @GetMapping("/reviewBoard")
    public String showReviewBoard(@RequestParam(value = "region", required = false, defaultValue = "전체") String regionName,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "5") int size,
                                  HttpServletRequest request,
                                  Model model) {
        Page<ReviewBoard> boardPage = reviewBoardService.getBoardPage(regionName, page, size);

        model.addAttribute("boards", boardPage.getContent()); // 현재 페이지의 게시글 목록
        model.addAttribute("region", regionName); // 선택된 지역
        model.addAttribute("currentPage", page); // 현재 페이지 번호
        model.addAttribute("totalPages", boardPage.getTotalPages()); // 전체 페이지 수
        model.addAttribute("pageSize", size);

        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);

        return "board/reviewBoard";
    }

    // 게시글 작성
    @GetMapping("/write")
    public String write(@RequestParam(value = "region", required = false) String region,
                        HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("region", region);

        return "board/write";
    }

    @PostMapping("/reviewBoard/save")
    public ResponseEntity<?> saveReviewBoard(@ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                             @AuthenticationPrincipal SiteUser currentUser,
                                             @RequestParam("region") String region,
                                             @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
                                             BindingResult bindingResult) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }
        // 지역 정보 처리 및 URL 인코딩
        String encodedRegion = processRegionAndEncode(region, reviewBoardDTO);
        // 게시글 생성
        ReviewBoard savedBoard = reviewBoardService.createReviewBoard(reviewBoardDTO, currentUser);

        return ResponseEntity.ok(Map.of("success", true, "boardId", savedBoard.getId(), "region", encodedRegion));
    }

    // 지역 정보와 URL 인코딩을 처리하는 메서드
    private String processRegionAndEncode(String region, ReviewBoardDTO reviewBoardDTO) {
        Region selectedRegion = regionRepository.findByRegionName(region)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

        reviewBoardDTO.setRegion(selectedRegion); // DTO에 지역 설정

        // URL 인코딩 적용
        try {
            return URLEncoder.encode(region, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return region; // 인코딩 오류 발생 시 원래 값 사용
        }
    }

    private String getMainImageUrl(List<String> uploadedImages, int mainImageIndex) {
        if (uploadedImages == null || uploadedImages.isEmpty()) {
            return "/static/images/default-thumbnail.png";
        }
        int index = Math.min(mainImageIndex, uploadedImages.size() - 1);
        return uploadedImages.get(index);
    }

    // 게시글 상세 페이지 조회
    @GetMapping("/detail/{id}")
    public String showBoardDetail(@PathVariable("id") Long id,
                                  @RequestParam(name = "region", required = false, defaultValue = "전체") String regionName,
                                  @AuthenticationPrincipal SiteUser currentUser,
                                  Model model) {
        ReviewBoard board = reviewBoardService.getBoardById(id);

        if (board == null) {
            return "redirect:/board/reviewBoard?region=" + regionName;
        }

        List<ImageBoard> images = board.getReview_images();

        // 좋아요 상태 - user가 로그인 안 했을 때 false 처리
        Boolean likeStatus = false;
        if (currentUser != null) {
            likeStatus = reviewBoardLikeService.hasUserLiked(id, currentUser);
        }

        // 좋아요 개수 - null 방어 처리
        Long likeCount = reviewBoardLikeService.countLikes(id);

        model.addAttribute("board", board);
        model.addAttribute("region", regionName);
        model.addAttribute("images", images);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("likeStatus", likeStatus != null ? likeStatus : false);
        model.addAttribute("likeCount", likeCount != null ? likeCount : 0);

        return "board/detail";
    }

    // 게시글 수정
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail/{id}/update")
    public String update(@PathVariable Long id, Model model, HttpServletRequest request) {
        ReviewBoard board = reviewBoardService.getBoardById(id);

        if (board == null) {
            return "redirect:/board/reviewBoard";
        }
        List<String> imageUrls = reviewBoardService.getImageUrlsByBoardId(id);

        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("board", board);
        model.addAttribute("imageUrls", imageUrls);

        return "board/update";
    }

    // 게시글 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviewBoard/update")
    public String updateReviewBoard(@ModelAttribute @Valid ReviewBoardDTO reviewBoardDTO,
                                    @RequestParam(value = "files[]", required = false) List<MultipartFile> files,
                                    @RequestParam(value = "imageIdsToDelete", required = false) List<Long> imageIdsToDelete,
                                    BindingResult bindingResult, Model model, HttpServletRequest request) throws IOException {

        if (bindingResult.hasErrors()) {
            ReviewBoard board = reviewBoardService.getBoardById(reviewBoardDTO.getId());
            model.addAttribute("board", board);
            model.addAttribute("_csrf", csrfTokenRepository.generateToken(request));
            return "board/update";
        }
        reviewBoardService.updateReviewBoard(reviewBoardDTO, files, imageIdsToDelete);

        return "redirect:/board/detail/" + reviewBoardDTO.getId();
    }

    @PreAuthorize("isAuthenticated() and (principal.id == #currentUser.id or hasRole('ADMIN'))")
    @DeleteMapping("/reviewBoard/delete/{id}")
    public ResponseEntity<Void> deleteReviewBoard(@PathVariable Long id,
                                                  @AuthenticationPrincipal SiteUser currentUser) {
        logger.info("Current user id: {}", currentUser != null ? currentUser.getId() : "null");
        logger.info("Attempting to delete post with id: {}", id);
        reviewBoardService.deleteReviewBoard(id);
        return ResponseEntity.ok().build();
    }

    // 문자열을 Region 객체로 변환하는 기능
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Region.class, "region", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text != null && !text.isEmpty()) {
                    Region region = regionRepository.findByRegionName(text)
                            .orElseThrow(() -> new IllegalArgumentException("This region does not exist."));
                    setValue(region);
                } else {
                    setValue(null);
                }
            }
        });
    }
}
