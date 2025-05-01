package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionDetailResponse;
import com.example.travelProj.domain.attraction.AttractionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attraction")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;
    private final RegionCodeService regionCodeService;

    // 지역명으로 검색 > 내부에서 지역코드 변환
    @GetMapping("/search")
    public List<AttractionResponse> searchAttractionByRegionName(
            @RequestParam String regionName,
            @RequestParam(required = false, defaultValue = "12") Integer contentTypeId) {  // 관광지 타입 파라미터 추가
        String regionCode = regionCodeService.findAreaCodeByKeyword(regionName);
        if (regionCode == null) {
            return List.of();  // 또는 적절한 오류 메시지를 반환할 수 있음
        }

        // 지역 코드와 선택된 관광지 타입으로 관광지 검색
        return apiService.searchAttractionByRegion(regionCode, contentTypeId.toString());
    }

    // 관광지 상세정보를 가져오는 API
    @GetMapping("/detail/{contentId}/{contentTypeId}")
    public ResponseEntity<AttractionDetailResponse> getAttractionDetail(
            @PathVariable Long contentId,
            @PathVariable String contentTypeId,
            @RequestParam String areaCode) {
        AttractionDetailResponse detail = apiService.fetchDetailInfo(contentId, contentTypeId, areaCode);
        if (detail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(detail);
    }

}
