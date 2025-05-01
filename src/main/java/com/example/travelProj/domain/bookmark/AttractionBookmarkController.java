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
                                         @RequestParam String areaCode) {
        try {
            attractionBookmarkService.addBookmark(attractionId, user, contentTypeId, areaCode);
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
        // 사용자가 북마크한 관광지 목록을 가져옴
        List<AttractionResponse> bookmarkedAttractions = bookmarkApiService.getUserBookmarkedAttractions(user);
        System.out.println("Bookmarked attractions: " + bookmarkedAttractions);

        // 각 관광지에 대한 상세 정보를 비동기 방식으로 조회 (병렬 처리)
        List<CompletableFuture<AttractionDetailResponse>> detailFutures = bookmarkedAttractions.stream()
                .map(attractionResponse ->
                        CompletableFuture.supplyAsync(() ->
                                apiService.fetchDetailInfo(attractionResponse.getContentId(), attractionResponse.getContentTypeId(), attractionResponse.getAreaCode())
                        )
                )
                .collect(Collectors.toList());

        // 모든 비동기 작업을 기다리고 결과를 반환
        List<AttractionDetailResponse> bookmarkDetails = detailFutures.stream()
                .map(CompletableFuture::join)  // join()을 사용하여 각 작업의 결과를 기다림
                .collect(Collectors.toList());

        System.out.println("Bookmark details: " + bookmarkDetails);

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
        List<AttractionResponse> bookmarkedAttractions = bookmarkApiService.getUserBookmarkedAttractions(user);

        // 각 관광지에 대한 상세 정보를 조회
        List<AttractionDetailResponse> bookmarkDetails = bookmarkedAttractions.stream()
                .map(attractionResponse -> apiService.fetchDetailInfo(attractionResponse.getContentId(), attractionResponse.getContentTypeId(), attractionResponse.getAreaCode()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookmarkDetails);
    }



}

