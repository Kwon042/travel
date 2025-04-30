package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.attraction.AttractionDetailResponse;
import com.example.travelProj.domain.attraction.AttractionResponse;
import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.user.SiteUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        attractionBookmarkRepository.save(new AttractionBookmark(user, attractionId));
    }

    // 즐겨찾기 제거
    public void removeBookmark(Long attractionId, SiteUser user) {
        // 북마크가 존재하는 경우에만 삭제
        attractionBookmarkRepository.deleteByAttractionIdAndUser(attractionId, user);
    }

    // 즐겨찾기 여부 확인
    public boolean isBookmarked(Long attractionId, SiteUser user) {
        return attractionBookmarkRepository.existsByAttractionIdAndUser(attractionId, user);
    }

    // 북마크 개수 조회
    public long getBookmarkCount(Long attractionId) {
        return attractionBookmarkRepository.countByAttractionId(attractionId);
    }

    // 외부 API에서 관광지 정보 가져오기
    public AttractionResponse getAttraction(Long attractionId) {
        // `attractionId`를 사용하여 외부 API에서 관광지 정보를 가져옵니다.
        return apiService.searchAttractionByRegion(attractionId.toString(), null).stream()
                .filter(attraction -> attraction.getContentId().equals(attractionId))
                .findFirst()
                .orElse(null); // 찾은 관광지 정보가 없으면 null 반환
    }

    // 관광지 상세 정보 가져오기
    public AttractionDetailResponse getAttractionDetails(Long attractionId) {
        // "12"는 관광지 유형 ID 예시로, 실제 사용 시 적절한 contentTypeId로 수정해야 합니다.
        return apiService.fetchDetailInfo(attractionId, "12");
    }
}
