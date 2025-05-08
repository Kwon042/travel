package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.api.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attractions")
public class AttractionController {

    private final AttractionService attractionService;
    private final ApiService apiService;

    // 지역 코드(areaCode)와 contentTypeId를 받아서 지역기반 관광지 검색
    @GetMapping("/search")
    public List<AttractionResponse> searchAttractionsByRegion(
            @RequestParam String areaCode,  // 지역 코드 (regionName 대신)
            @RequestParam(required = false, defaultValue = "12") Integer contentTypeId) {  // 관광지 타입 파라미터 추가

        // ApiService를 사용하여 관광지 검색
        return apiService.searchAttractionByRegion(areaCode, contentTypeId.toString());
    }
}

//    // 상세 정보 가져오기
//    @GetMapping("/{contentId}")
//    public ResponseEntity<AttractionDetailResponse> getAttractionDetail(
//            @PathVariable Long contentId,
//            @RequestParam String contentTypeId,
//            @RequestParam String areaCode) throws IOException {
//
//        AttractionDetailResponse detail = attractionService.getAttractionDetail(contentId, contentTypeId, areaCode);
//        return ResponseEntity.ok(detail);
//    }


