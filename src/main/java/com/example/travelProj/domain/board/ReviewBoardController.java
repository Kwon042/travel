package com.example.travelProj.domain.board;

import com.example.travelProj.domain.image.Image;
import com.example.travelProj.domain.region.Region;
import com.example.travelProj.domain.region.RegionRepository;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class ReviewBoardController {

    private final CsrfTokenRepository csrfTokenRepository;
    private final RegionRepository regionRepository;
    private final ReviewBoardService reviewBoardService;

    // 리뷰 게시판 목록
    @GetMapping("/reviewBoard")
    public String showReviewBoard(@RequestParam(value = "region", required = false, defaultValue = "전체") String regionName,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "5") int size,
                                  Model model) {
        Page<ReviewBoard> boardPage = reviewBoardService.getBoardPage(regionName, page, size);

        model.addAttribute("boards", boardPage.getContent()); // 현재 페이지의 게시글 목록
        model.addAttribute("region", regionName); // 선택된 지역
        model.addAttribute("currentPage", page); // 현재 페이지 번호
        model.addAttribute("totalPages", boardPage.getTotalPages()); // 전체 페이지 수
        model.addAttribute("pageSize", size);

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

    // 새 게시글 저장
    @PostMapping("/reviewBoard/save")
    public String saveReviewBoard(@ModelAttribute ReviewBoardDTO reviewBoardDTO,
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
    public String showBoardDetail(@PathVariable("id") Long id,
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
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/detail/{id}/update")
    public String update(@PathVariable Long id, Model model, HttpServletRequest request) {
        ReviewBoard board = reviewBoardService.getBoardById(id);

        if (board == null) {
            return "redirect:/board/reviewBoard";
        }

        model.addAttribute("board", board);
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        model.addAttribute("_csrf", csrfToken);
        return "board/update";
    }

    // 게시글 수정
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reviewBoard/update")
    public String updateReviewBoard(@ModelAttribute @Valid ReviewBoardDTO reviewBoardDTO,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    BindingResult bindingResult, Model model, HttpServletRequest request) throws IOException {

        if (bindingResult.hasErrors()) {
            ReviewBoard board = reviewBoardService.getBoardById(reviewBoardDTO.getId());
            model.addAttribute("board", board);
            model.addAttribute("_csrf", csrfTokenRepository.generateToken(request));
            return "board/update";
        }

        // 서비스에서 모든 작업 처리
        reviewBoardService.updateReviewBoard(reviewBoardDTO, file);
        return "redirect:/board/detail/" + reviewBoardDTO.getId();
    }

    @PreAuthorize("isAuthenticated() and (principal.id == #currentUser.id or hasRole('ADMIN'))")
    @DeleteMapping("/reviewBoard/delete/{id}")
    public ResponseEntity<Void> deleteReviewBoard(@PathVariable Long id, @AuthenticationPrincipal SiteUser currentUser) {
        boolean isDeleted = reviewBoardService.deleteReviewBoard(id, currentUser);

        if (isDeleted) {
            return ResponseEntity.ok().build(); // 삭제 성공
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
        }
    }

    // 문자열을 Region 객체로 변환하는 기능
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Region.class, "region", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text != null && !text.isEmpty()) {
                    Region region = regionRepository.findByRegionName(text)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
                    setValue(region);
                } else {
                    setValue(null);
                }
            }
        });
    }
}
