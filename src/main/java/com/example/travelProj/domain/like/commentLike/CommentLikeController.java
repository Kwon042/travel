package com.example.travelProj.domain.like.commentLike;

import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment/likes")
public class CommentLikeController {
    private final CommentLikeService likeService;

    @PostMapping("/{commentId}")
    public ResponseEntity<?> addLike(@PathVariable Long commentId, @AuthenticationPrincipal SiteUser user) {
        likeService.addLike(commentId, user);
        return ResponseEntity.ok().body("Like added successfully.");
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> removeLike(@PathVariable Long commentId, @AuthenticationPrincipal SiteUser user) {
        likeService.removeLike(commentId, user);
        return ResponseEntity.ok().body("Like removed successfully.");
    }

    @GetMapping("/{commentId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long commentId) {
        long count = likeService.countLikes(commentId);
        return ResponseEntity.ok(count);
    }
}
