package com.example.travelProj.domain.bookmark;

import com.example.travelProj.domain.user.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttractionBookmarkRepository extends JpaRepository<AttractionBookmark, Long> {
    List<AttractionBookmark> findByUser(SiteUser user);
    // 특정 관광지에 대한 북마크 개수 조회
    long countByAttractionId(Long attractionId);
    boolean existsByAttractionIdAndUser(Long attractionId, SiteUser user);
    void deleteByAttractionIdAndUserAndContentTypeIdAndAreaCode(Long attractionId, SiteUser user, String contentTypeId, String areaCode);
}
