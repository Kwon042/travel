package com.example.travelProj.domain.like.boardlike;

import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviewBoard/likes")
public class ReviewBoardLikeController {

    private final ReviewBoardLikeService reviewBoardLikeService;

    // 좋아요 클릭
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{reviewBoardId}")
    public ResponseEntity<?> addLike(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        reviewBoardLikeService.addLike(reviewBoardId, user);
        return ResponseEntity.ok().body("Like added successfully.");
    }

    // 좋아요 제거
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{reviewBoardId}")
    public ResponseEntity<?> removeLike(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        reviewBoardLikeService.removeLike(reviewBoardId, user);
        return ResponseEntity.ok().body("Like removed successfully.");
    }

    // 좋아요 수 조회
    @GetMapping("/{reviewBoardId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long reviewBoardId) {
        long count = reviewBoardLikeService.countLikes(reviewBoardId);
        return ResponseEntity.ok(count);
    }

    // 관리자만 좋아요 목록 조회 가능
    @GetMapping("/{reviewBoardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getLikeUsers(@PathVariable Long reviewBoardId) {
        List<UserResponseDTO> likeUsers = reviewBoardLikeService.getLikeUsers(reviewBoardId);
        return ResponseEntity.ok(likeUsers);
    }

    // 현재 로그인한 사용자가 해당 게시글 좋아요 눌렀는지 확인
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{reviewBoardId}/status")
    public ResponseEntity<Boolean> checkLikeStatus(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        boolean hasLiked = reviewBoardLikeService.hasUserLiked(reviewBoardId, user);
        return ResponseEntity.ok(hasLiked);
    }
}
