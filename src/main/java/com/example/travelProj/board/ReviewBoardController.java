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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
                        //@RequestParam(value = "boardType", required = true) String boardType,
                        HttpServletRequest request, Model model) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        //model.addAttribute("boardType", boardType);
        model.addAttribute("region", region);

        return "board/write";
    }

    @GetMapping("/reviewBoard/update/{id}")
    public String update(@PathVariable Long id, Model model, HttpServletRequest request) {
        ReviewBoard board = reviewBoardService.getBoardById(id);
        // 게시글을 불러와서 모델에 추가
        model.addAttribute("board", board);
        model.addAttribute("_csrf", csrfTokenRepository.generateToken(request)); // CSRF Token 추가

        return "board/write"; // 수정 폼 페이지로 이동
    }

    // 새 게시글 저장
    @PostMapping("/reviewBoard/save")
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

        // URL 인코딩 적용
        try {
            String encodedRegion = URLEncoder.encode(region, StandardCharsets.UTF_8.toString());
            return "redirect:/board/reviewBoard?region=" + encodedRegion;
        } catch (UnsupportedEncodingException e) {
            // 인코딩 실패 시 예외 처리, 로그 남기기 등
            e.printStackTrace();
            return "redirect:/board/reviewBoard?region=" + region; // 인코딩 실패시 원래 값으로 리다이렉트
        }
    }

    // 게시글 상세 페이지 조회
    @GetMapping("/detail/{id}")
    public String showBoardDetail(@PathVariable Long id,
                                  @RequestParam(name = "region", required = false, defaultValue = "전체") String regionName,
                                  @AuthenticationPrincipal SiteUser currentUser,
                                  Model model) {
        ReviewBoard board = reviewBoardService.getBoardById(id);

        if (board == null) {
            return "redirect:/board/reviewBoard?region=" + regionName;
        }

        List<Image> images = board.getReview_images();

        model.addAttribute("board", board);
        model.addAttribute("region", regionName);
        model.addAttribute("images", images);
        model.addAttribute("currentUser", currentUser);


        return "board/detail";
    }

    // 게시글 수정
    @PostMapping("/reviewBoard/update/{id}")
    public String updateReviewBoard(@PathVariable Long id,
                                    @ModelAttribute ReviewBoardDTO reviewBoardDTO,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    BindingResult bindingResult, Model model, HttpServletRequest request) throws IOException {
        System.out.println("Received title: " + reviewBoardDTO.getTitle());

        if (bindingResult.hasErrors()) {
            // 에러가 있는 경우 수정할 게시글 정보 불러오기
            ReviewBoard board = reviewBoardService.getBoardById(id);

            if (board != null) {
                model.addAttribute("board", board); // 기존 게시글 데이터 추가
                model.addAttribute("_csrf", csrfTokenRepository.generateToken(request));
            }
            // 수정 폼으로 돌아가기
            return "board/write";
        }
        reviewBoardService.updateReviewBoard(id, reviewBoardDTO, file);
        return "redirect:/board/detail/" + id + "?region=" + reviewBoardDTO.getRegion().getRegionName();
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
