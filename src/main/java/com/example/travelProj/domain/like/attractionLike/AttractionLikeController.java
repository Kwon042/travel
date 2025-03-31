package com.example.travelProj.domain.like.attractionLike;

import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attraction/likes")
public class AttractionLikeController {
    private final AttractionLikeService likeService;

    @PostMapping("/{attractionId}")
    public ResponseEntity<?> addLike(@PathVariable Long attractionId, @AuthenticationPrincipal SiteUser user) {
        likeService.addLike(attractionId, user);
        return ResponseEntity.ok().body("Like added successfully.");
    }

    @DeleteMapping("/{attractionId}")
    public ResponseEntity<?> removeLike(@PathVariable Long attractionId, @AuthenticationPrincipal SiteUser user) {
        likeService.removeLike(attractionId, user);
        return ResponseEntity.ok().body("Like removed successfully.");
    }

    @GetMapping("/{attractionId}/count")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long attractionId) {
        long count = likeService.countLikes(attractionId);
        return ResponseEntity.ok(count);
    }
}
