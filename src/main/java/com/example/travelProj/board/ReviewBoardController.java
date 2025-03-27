package com.example.travelProj.board;

import com.example.travelProj.Image;
import com.example.travelProj.Region;
import com.example.travelProj.RegionRepository;
import com.example.travelProj.user.SiteUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class ReviewBoardController {

    private final CsrfTokenRepository csrfTokenRepository;
    private final RegionRepository regionRepository;
    private final ReviewBoardService reviewBoardService;

    // 리뷰 게시판 목록
    @GetMapping("/reviewBoard")
    public String showReviewBoard(@RequestParam(value  = "region", required = false, defaultValue = "전체") String regionName, Model model) {
        System.out.println("Received region: " + regionName);

        List<ReviewBoard> boards;

        if (regionName.equals("전체")) {
            boards = reviewBoardService.getAllBoards();
        } else {
            System.out.println("Received region: " + regionName);
            Region region = reviewBoardService.findByRegionName(regionName)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
            boards = reviewBoardService.getBoardsByRegion(region);
        }

        model.addAttribute("boards", boards);
        model.addAttribute("region", regionName);
        return "board/reviewBoard";
    }

    // 게시글 작성
    @GetMapping("/write")
    public String write(@RequestParam(value = "region", required = false) String region,
                        @RequestParam(value = "boardType", required = true) String boardType,
                        HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        model.addAttribute("boardType", boardType);

        // 전체 게시판인지 확인
        boolean isAllBoard = "reviewBoard".equals(boardType);
        model.addAttribute("isAllBoard", isAllBoard);

        model.addAttribute("region", region);

        return "board/write";
    }

    // 게시글 저장
    @PostMapping("/reviewBoard")
    public String createReviewBoard(@ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                    @AuthenticationPrincipal SiteUser currentUser,
                                    @RequestParam String region) {
        // 게시글 생성 호출
        reviewBoardDTO.setRegion(selectedRegion);
        reviewBoardService.createReviewBoard(reviewBoardDTO, currentUser);

        return "redirect:/board/reviewBoard?region=" + region;
    }

    @PostMapping("/save")
    public String saveReviewBoard(@ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                  @AuthenticationPrincipal SiteUser currentUser,
                                  BindingResult bindingResult) {
        // 사용자가 로그인되어 있는지 확인
        if (currentUser == null) {
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        }

        if (bindingResult.hasErrors()) {
            // 오류가 있을 경우 기존 입력 폼으로 돌아가기
            return "redirect:/user/mypage?error"; // 오류가 발생했을 때 처리
        }

        try {
            reviewBoardService.createReviewBoard(reviewBoardDTO, currentUser);
        } catch (Exception e) {
            e.printStackTrace();
            // 예외 발생 시 오류 메시지를 적절하게 처리
            return "redirect:/user/mypage?error=" + e.getMessage();
        }
        return "redirect:/board/reviewBoard";
    }

    // 게시글 상세 페이지 조회
    @GetMapping("/detail/{id}")
    public String showBoardDetail(@PathVariable Long id,
                                  @RequestParam(name = "region", required = false, defaultValue = "전체") String regionName,
                                  Model model) {
        // ID에 따라 게시글 조회
        ReviewBoard board = reviewBoardService.getBoardById(id);

        // 게시글이 존재하지 않을 경우 예외 처리 (목록 페이지로 리다이렉트)
        if (board == null) {
            return "redirect:/board/reviewBoard";
        }

        // 게시글의 이미지 리스트 가져오기
        List<Image> images = board.getReview_images();

        model.addAttribute("region", regionName);
        model.addAttribute("board", board);
        model.addAttribute("images", images);

        return "board/detail";
    }

    // 게시글 수정
    @PostMapping("/reviewBoard/update/{id}")
    public String updateReviewBoard(@PathVariable Long id,
                                    @ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    @RequestParam(name = "region", required = false, defaultValue = "전체") String regionName) throws IOException {
        reviewBoardService.updateReviewBoard(id, reviewBoardDTO, file);
        // 수정 후 해당 게시글 상세 페이지로 리다이렉트하면서 region도 함께 전달
        return "redirect:/board/detail/" + id + "?region=" + regionName;
    }
}
