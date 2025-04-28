package com.example.travelProj.domain.like.commentlike;

import com.example.travelProj.domain.comment.CommentService;
import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment/likes")
public class CommentLikeController {
    private final CommentLikeService commentLikeService;

    @PostMapping("/{commentId}")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long commentId, @AuthenticationPrincipal SiteUser user) {
        boolean likeStatus = commentLikeService.toggleLike(commentId, user);  // 좋아요 상태 토글
        long likeCount = commentLikeService.countLikes(commentId);  // 좋아요 수

        Map<String, Object> response = new HashMap<>();
        response.put("likeStatus", likeStatus);  // 좋아요 상태
        response.put("likesCount", likeCount);  // 좋아요 수

        return ResponseEntity.ok(response);  // JSON 응답 반환
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> removeLike(@PathVariable Long commentId, @AuthenticationPrincipal SiteUser user) {
        commentLikeService.removeLike(commentId, user);
        return ResponseEntity.ok().body("Like removed successfully.");
    }

    @GetMapping("/{commentId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long commentId) {
        long count = commentLikeService.countLikes(commentId);
        return ResponseEntity.ok(count);
    }
}
