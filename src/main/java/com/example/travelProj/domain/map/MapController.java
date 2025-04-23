package com.example.travelProj.domain.map;

import com.example.travelProj.domain.attraction.AttractionResponse;
import com.example.travelProj.domain.attraction.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
// 웹 페이지에서 지도를 보여줄 때 필요
public class MapController {

    private final AttractionService attractionService;

    @GetMapping("/map")
    public String showMapPage() {
        return "map";
    }

    // /api/attractions 요청 시 JSON 데이터 반환 (js에서)
    @GetMapping("/api/attractions")
    @ResponseBody
    public List<AttractionResponse> getAttractionsForMap(@RequestParam String region) {
        // AttractionService를 사용하여 관광지 데이터를 조회
        return attractionService.getAttractionsForMap(region);
    }
}
