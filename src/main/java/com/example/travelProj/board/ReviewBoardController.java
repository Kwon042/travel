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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyEditorSupport;
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
    public String showReviewBoard(@RequestParam(value  = "region", required = false, defaultValue = "전체") String regionName,
                                  Model model) {
        System.out.println("Received region: " + regionName);
        List<ReviewBoard> boards;

        // 모든 게시글 조회
        if (regionName.equals("전체")) {
            boards = reviewBoardService.getAllBoards();
        } else {
            Region region = reviewBoardService.findByRegionName(regionName)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
            // 특정 지역 게시글 조회
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
                                    @RequestParam String region, BindingResult bindingResult) {
        if (currentUser == null) {
            return "redirect:/user/login";
        }
        if (bindingResult.hasErrors()) {
            return "board/write";
        }
        // 지역 정보를 가져오기
        Region selectedRegion = reviewBoardService.findByRegionName(region)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

        reviewBoardDTO.setRegion(selectedRegion);
        reviewBoardService.createReviewBoard(reviewBoardDTO, currentUser);

        return "redirect:/board/reviewBoard?region=" + region;
    }

    @PostMapping("/save")
    public String saveReviewBoard(@ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                  @AuthenticationPrincipal SiteUser currentUser,
                                  BindingResult bindingResult) {
        if (currentUser == null) {
            return "redirect:/user/login";
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/user/mypage?error";
        }

        try {
            // region 필드에서 지역 이름을 가져와 찾아서 설정
            String regionName = reviewBoardDTO.getRegion().getRegionName();
            Region selectedRegion = reviewBoardService.findByRegionName(regionName)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));

            reviewBoardDTO.setRegion(selectedRegion);
            reviewBoardService.createReviewBoard(reviewBoardDTO, currentUser);
        } catch (Exception e) {
            e.printStackTrace();
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

    // 문자열을 Region 객체로 변환하는 기능
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Region.class, "region",
                new PropertyEditorSupport() {
                    @Override
                    public void setAsText(String text) {
                        Region region = regionRepository.findByRegionName(text)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
                        setValue(region); // 변환된 지역 객체 설정
                    }
                });
    }
}
