package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attraction")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;
    private final RegionCodeService regionCodeService;

    // 지역명으로 검색 > 내부에서 지역코드 변환
    @GetMapping("/search")
    public List<AttractionResponse> searchAttractionByRegionName(@RequestParam String regionName) {
        String regionCode = regionCodeService.findAreaCodeByKeyword(regionName);
        if (regionCode == null) {
            return List.of();  // 또는 적절한 오류 메시지를 반환할 수 있음
        }
        // 지역 코드로 관광지 검색
        return apiService.searchAttractionByRegion(regionCode);
    }

}
