package com.example.travelProj.domain.like.boardlike;

import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviewBoard/likes")
public class ReviewBoardLikeController {

    private final ReviewBoardLikeService reviewBoardLikeService;

    // 좋아요 클릭
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{reviewBoardId}")
    public ResponseEntity<Map<String, Object>> addLike(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        try {
            reviewBoardLikeService.addLike(reviewBoardId, user);

            // 좋아요가 성공적으로 추가된 후 JSON 응답을 반환
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Like added successfully.");
            response.put("hasLiked", true);  // 현재 좋아요 상태
            response.put("likeCount", reviewBoardLikeService.countLikes(reviewBoardId));  // 좋아요 수

            return ResponseEntity.ok(response);  // JSON 응답
        } catch (IllegalStateException e) {
            // 충돌 발생 시, 409 상태와 함께 메시지를 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(409).body(errorResponse);  // JSON 응답
        }
    }

    // 좋아요 제거
    @DeleteMapping("/{reviewBoardId}")
    public ResponseEntity<Map<String, Object>> removeLike(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        reviewBoardLikeService.removeLike(reviewBoardId, user);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Like removed successfully.");
        response.put("hasLiked", false);  // 좋아요 해제됨
        response.put("likeCount", reviewBoardLikeService.countLikes(reviewBoardId));  // 좋아요 수 반환

        return ResponseEntity.ok(response);
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
    public ResponseEntity<Boolean> checkLikeStatus(@PathVariable Long reviewBoardId,
                                                   @AuthenticationPrincipal SiteUser user) {
        boolean hasLiked = reviewBoardLikeService.hasUserLiked(reviewBoardId, user);
        return ResponseEntity.ok(hasLiked);
    }
}
