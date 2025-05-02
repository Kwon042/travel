package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.attraction.AttractionDetailResponse;
import com.example.travelProj.domain.attraction.AttractionResponse;
import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class AttractionBookmarkController {

    private final AttractionBookmarkService attractionBookmarkService;
    private final BookmarkApiService bookmarkApiService;
    private final ApiService apiService;

    @PostMapping("/{attractionId}")
    public ResponseEntity<?> addBookmark(@PathVariable Long attractionId,
                                         @AuthenticationPrincipal SiteUser user,
                                         @RequestParam String contentTypeId,
                                         @RequestParam String areaCode,
                                         @RequestParam(required = false) String firstimage) {
        try {
            attractionBookmarkService.addBookmark(attractionId, user, contentTypeId, areaCode, firstimage);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();  // 예외 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while adding bookmark");
        }
    }

    @DeleteMapping("/{attractionId}")
    public ResponseEntity<?> removeBookmark(@PathVariable Long attractionId,
                                            @AuthenticationPrincipal SiteUser user,
                                            @RequestParam String contentTypeId,
                                            @RequestParam String areaCode) {
        try {
            attractionBookmarkService.removeBookmark(attractionId, user, contentTypeId, areaCode);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();  // 예외 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while removing bookmark");
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<AttractionDetailResponse>> getBookmarkedList(@AuthenticationPrincipal SiteUser user) {
        List<AttractionDetailResponse> bookmarkDetails = bookmarkApiService.getUserBookmarkedDetails(user);
        return ResponseEntity.ok(bookmarkDetails);
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

    @GetMapping("/my")
    public ResponseEntity<List<AttractionDetailResponse>> getMyBookmarks(@AuthenticationPrincipal SiteUser user) {
        List<AttractionDetailResponse> bookmarkDetails = bookmarkApiService.getUserBookmarkedDetails(user);

        return ResponseEntity.ok(bookmarkDetails);
    }




}

