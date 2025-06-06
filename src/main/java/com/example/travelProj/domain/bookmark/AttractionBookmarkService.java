package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.attraction.AttractionDetailResponse;
import com.example.travelProj.domain.attraction.AttractionResponse;
import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttractionBookmarkService {

    private final AttractionBookmarkRepository attractionBookmarkRepository;

    // 사용자가 북마크한 관광지 목록을 반환
    public List<AttractionBookmark> getUserBookmarkedAttractions(SiteUser user) {
        List<AttractionBookmark> bookmarks = attractionBookmarkRepository.findByUser(user);
        System.out.println("User's bookmarks: " + bookmarks);  // 북마크 정보 로그 출력
        return bookmarks;
    }

    // 즐겨찾기 추가
    public void addBookmark(Long attractionId, SiteUser user, String contentTypeId, String areaCode) {
        if (attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user)) return;
        // 북마크 추가
        attractionBookmarkRepository.save(new AttractionBookmark(user, attractionId, contentTypeId, areaCode));
    }

    // 즐겨찾기 제거
    @Transactional
    public void removeBookmark(Long attractionId, SiteUser user, String contentTypeId, String areaCode) {
        // 북마크가 존재하는 경우에만 삭제
        attractionBookmarkRepository.deleteByAttractionIdAndUserAndContentTypeIdAndAreaCode(attractionId, user, contentTypeId, areaCode);
    }

    // 즐겨찾기 여부 확인
    public boolean isBookmarked(Long attractionId, SiteUser user) {
        return attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user);
    }

    // 북마크 개수 조회
    public long getBookmarkCount(Long attractionId) {
        return attractionBookmarkRepository.countByAttractionId(attractionId);
    }
}
