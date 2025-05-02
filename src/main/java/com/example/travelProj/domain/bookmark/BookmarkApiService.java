package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.attraction.AttractionDetailResponse;
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
    public List<AttractionDetailResponse> getUserBookmarkedDetails(SiteUser user) {
        List<AttractionBookmark> bookmarks = attractionBookmarkService.getUserBookmarkedAttractions(user);

        return bookmarks.parallelStream()
                .map(bookmark -> apiService.fetchDetailInfo(
                        bookmark.getAttractionId(),
                        bookmark.getContentTypeId(),
                        bookmark.getAreaCode()
                ))
                .filter(detail -> detail != null)
                .collect(Collectors.toList());
    }



}

