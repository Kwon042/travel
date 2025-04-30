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
public class AttractionBookmarkService {

    private final AttractionBookmarkRepository attractionBookmarkRepository;
    private final ApiService apiService;

    // 즐겨찾기 추가
    public void addBookmark(Long attractionId, SiteUser user, String contentTypeId) {
        // 이미 북마크한 관광지인 경우 추가하지 않음
        if (attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user)) {
            return;
        }
        // 새로 북마크 추가
        attractionBookmarkRepository.save(new AttractionBookmark(user, attractionId, contentTypeId));
    }

    // 즐겨찾기 제거
    public void removeBookmark(Long attractionId, SiteUser user, String contentTypeId) {
        // 북마크가 존재하는 경우에만 삭제
        attractionBookmarkRepository.deleteByAttractionIdAndUserAndContentTypeId(attractionId, user, contentTypeId);
    }

    // 즐겨찾기 여부 확인
    public boolean isBookmarked(Long attractionId, SiteUser user) {
        return attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user);
    }

    // 북마크 개수 조회
    public long getBookmarkCount(Long attractionId) {
        return attractionBookmarkRepository.countByAttractionId(attractionId);
    }

    // 관광지 목록 조회 (사용자의 북마크 기반)
    public List<AttractionResponse> getUserBookmarkedAttractions(SiteUser user) {
        List<AttractionBookmark> bookmarks = attractionBookmarkRepository.findByUser(user);

        return bookmarks.stream()
                .map(bookmark -> apiService.searchAttractionByRegion(
                        bookmark.getAttractionId().toString(),
                        bookmark.getContentTypeId()))
                .flatMap(List::stream)
                .filter(attraction ->
                        bookmarks.stream().anyMatch(b ->
                                b.getAttractionId().equals(attraction.getContentId())))
                .collect(Collectors.toList());
    }

    // 단일 관광지 정보 조회
    public AttractionResponse getAttraction(Long attractionId, String contentTypeId) {
        return apiService.searchAttractionByRegion(attractionId.toString(), contentTypeId).stream()
                .filter(attraction -> attraction.getContentId().equals(attractionId))
                .findFirst()
                .orElse(null);
    }

    // 관광지 상세 정보 조회
    public AttractionDetailResponse getAttractionDetails(Long attractionId, String contentTypeId) {
        return apiService.fetchDetailInfo(attractionId, contentTypeId);
    }
}
