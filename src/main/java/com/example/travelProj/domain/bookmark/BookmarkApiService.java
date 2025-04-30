package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.attraction.AttractionResponse;
import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkApiService {

    private final ApiService apiService;
    private final AttractionBookmarkService attractionBookmarkService;

    // 사용자가 북마크한 관광지 목록을 반환
    public List<AttractionResponse> getUserBookmarkedAttractions(SiteUser user) {
        List<AttractionBookmark> bookmarks = attractionBookmarkService.getUserBookmarkedAttractions(user);
        // 북마크된 관광지의 attractionId와 contentTypeId 출력
        bookmarks.forEach(bookmark -> {
            System.out.println("Attraction ID: " + bookmark.getAttractionId());
            System.out.println("Content Type ID: " + bookmark.getContentTypeId());
        });


        // 북마크된 관광지들의 정보를 contentTypeId와 함께 API를 통해 조회
        return bookmarks.stream()
                .map(bookmark -> apiService.searchAttractionByRegion(bookmark.getAttractionId().toString(), bookmark.getContentTypeId()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}

