package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class AttractionBookmarkController {

    private final AttractionBookmarkService attractionBookmarkService;

    @PostMapping("/{attractionId}")
    public ResponseEntity<?> addBookmark(@PathVariable Long attractionId,
                                         @AuthenticationPrincipal SiteUser user,
                                         @PathVariable String contentId) {
        attractionBookmarkService.addBookmark(attractionId, user, contentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{attractionId}")
    public ResponseEntity<?> removeBookmark(@PathVariable Long attractionId,
                                            @AuthenticationPrincipal SiteUser user) {
        attractionBookmarkService.removeBookmark(attractionId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{attractionId}/status")
    public ResponseEntity<Boolean> isBookmarked(@PathVariable Long attractionId,
                                                @AuthenticationPrincipal SiteUser user) {
        boolean result = attractionBookmarkService.isBookmarked(attractionId, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{attractionId}/count")
    public ResponseEntity<Long> getBookmarkCount(@PathVariable Long attractionId) {
        long count = attractionBookmarkService.getBookmarkCount(attractionId);
        return ResponseEntity.ok(count);
    }
}

