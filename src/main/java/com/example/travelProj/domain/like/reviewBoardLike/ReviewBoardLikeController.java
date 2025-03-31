package com.example.travelProj.domain.like.reviewBoardLike;

import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviewBoard/likes")
public class ReviewBoardLikeController {

    private final ReviewBoardLikeService likeService;

    @PostMapping("/{reviewBoardId}")
    public ResponseEntity<?> addLike(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        likeService.addLike(reviewBoardId, user);
        return ResponseEntity.ok().body("Like added successfully.");
    }

    @DeleteMapping("/{reviewBoardId}")
    public ResponseEntity<?> removeLike(@PathVariable Long reviewBoardId, @AuthenticationPrincipal SiteUser user) {
        likeService.removeLike(reviewBoardId, user);
        return ResponseEntity.ok().body("Like removed successfully.");
    }

    @GetMapping("/{reviewBoardId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long reviewBoardId) {
        long count = likeService.countLikes(reviewBoardId);
        return ResponseEntity.ok(count);
    }
}
