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
    private final AttractionBookmarkRepository attractionBookmarkRepository;

    // 사용자가 북마크한 관광지 목록을 반환
    public List<AttractionResponse> getUserBookmarkedAttractions(SiteUser user) {
        // 사용자가 북마크한 관광지 리스트 가져오기
        List<AttractionBookmark> bookmarks = attractionBookmarkRepository.findByUser(user);

        // 북마크된 관광지들의 attractionId를 추출하여, 해당 관광지 정보 API를 통해 조회
        return bookmarks.stream()
                .map(bookmark -> apiService.searchAttractionByRegion(bookmark.getAttractionId().toString()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    // 관광지를 북마크
    public boolean addBookmark(SiteUser user, Long attractionId, String contentTypeId) {
        // 이미 북마크된 관광지인지 확인
        if (attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user)) {
            return false;  // 이미 북마크된 관광지일 경우 추가하지 않음
        }

        // 북마크 추가
        AttractionBookmark bookmark = new AttractionBookmark(user, attractionId);
        attractionBookmarkRepository.save(bookmark);
        return true;
    }

    // 관광지 북마크 삭제
    public boolean removeBookmark(SiteUser user, Long attractionId) {
        // 북마크된 관광지가 존재하는지 확인
        if (!attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user)) {
            return false;  // 북마크가 없으면 삭제할 수 없음
        }

        // 북마크 삭제
        attractionBookmarkRepository.deleteByAttractionIdAndUser(attractionId, user);
        return true;
    }
}
