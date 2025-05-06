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
                .map(bookmark -> {
                    // 상세 정보 요청
                    AttractionDetailResponse detail = apiService.fetchDetailInfo(
                            bookmark.getAttractionId(),
                            bookmark.getContentTypeId(),
                            bookmark.getAreaCode()
                    );

                    if (detail != null && (detail.getFirstimage() == null || detail.getFirstimage().trim().isEmpty())) {
                        // 👉 firstimage가 없을 경우 지역기반 API에서 해당 ID의 이미지를 찾아 사용
                        List<AttractionResponse> candidates = apiService.searchAttractionByRegion(
                                bookmark.getAreaCode(),
                                bookmark.getContentTypeId()
                        );

                        // contentId가 일치하는 Attraction 찾기
                        AttractionResponse match = candidates.stream()
                                .filter(attraction -> attraction.getContentId().equals(bookmark.getAttractionId()))
                                .findFirst()
                                .orElse(null);

                        if (match != null && match.getFirstimage() != null && !match.getFirstimage().trim().isEmpty()) {
                            detail.setFirstimage(match.getFirstimage());
                        } else {
                            // 마지막으로 기본 이미지 지정
                            detail.setFirstimage("/images/no-image.png");
                        }
                    }

                    return detail;
                })
                .filter(detail -> detail != null)
                .collect(Collectors.toList());
    }

}


