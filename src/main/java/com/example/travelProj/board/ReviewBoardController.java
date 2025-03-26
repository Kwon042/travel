package com.example.travelProj.board;

import com.example.travelProj.user.SiteUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class ReviewBoardController {

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    private final ReviewBoardService reviewBoardService;

    @GetMapping("/reviewBoard")
    public String showReviewBoard(@RequestParam(name = "region", required = false) String region, Model model) {
        List<ReviewBoard> boards;
        if (region == null || region.isEmpty()) {
            boards = reviewBoardService.getAllBoards();
        } else {
            boards = reviewBoardService.getBoardsByRegion(region);
        }
        model.addAttribute("boards", boards);
        model.addAttribute("region", region);
        return "board/reviewBoard"; // 뷰 반환
    }

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

    @PostMapping("/reviewBoard")
    public String createReviewBoard(@ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                    @AuthenticationPrincipal SiteUser currentUser) {
        // 게시글 생성 호출
        reviewBoardService.createReviewBoard(reviewBoardDTO, currentUser);
        return "redirect:/board/reviewBoard";
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

    @GetMapping("/detail/{id}")
    public String showBoardDetail(@PathVariable Long id, Model model) {
        // ID에 따라 게시글 조회
        ReviewBoard board = reviewBoardService.getBoardById(id);

        // 게시글이 존재하지 않을 경우 예외 처리 (또는 다른 로직)
        if (board == null) {
            // 적절한 에러 처리 로직 추가 (예: 404 페이지로 리다이렉트)
            return "redirect:/error"; // 에러 페이지로 리다이렉트
        }

        model.addAttribute("board", board); // 상세 게시글을 모델에 추가
        return "board/detail"; // 상세 뷰로 이동
    }

}