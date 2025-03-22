package com.example.travelProj.board;

import com.example.travelProj.Image;
import com.example.travelProj.user.SiteUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
            boards = reviewBoardService.getAllBoards(); // 전체 게시글 가져오기
        } else {
            boards = reviewBoardService.getBoardsByRegion(region); // 특정 지역 게시글 가져오기
        }
        model.addAttribute("boards", boards);
        model.addAttribute("region", region); // 지역 정보 추가
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
    public String createReviewBoard(@RequestParam String title,
                                    @RequestParam String content,
                                    @RequestParam String region,
                                    @AuthenticationPrincipal SiteUser currentUser) {
        // 게시글 생성 호출
        reviewBoardService.createReviewBoard(title, content, region, currentUser);
        return "redirect:/reviewBoard"; // 생성 후 게시글 목록으로 리다이렉트
    }

}