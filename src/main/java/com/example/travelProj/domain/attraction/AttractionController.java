package com.example.travelProj.domain.attraction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("/attractions")
public class AttractionController {

    private final AttractionService attractionService;

    // 상세 정보 가져오기
    @GetMapping("/{contentId}")
    public AttractionDetailResponse getAttractionDetail(
            @RequestParam Long contentId,
            @RequestParam String contentTypeId,
            @RequestParam String areaCode) throws IOException {
        // AttractionService에서 상세 정보를 가져옴
        return attractionService.getAttractionDetail(contentId, contentTypeId, areaCode);
    }
}
