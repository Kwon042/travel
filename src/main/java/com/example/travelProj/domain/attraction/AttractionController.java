package com.example.travelProj.domain.attraction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("/api/attractions")
public class AttractionController {

    private final AttractionService attractionService;

    // 상세 정보 가져오기
    @GetMapping("/{contentId}")
    public ResponseEntity<AttractionDetailResponse> getAttractionDetail(
            @PathVariable Long contentId,
            @RequestParam String contentTypeId,
            @RequestParam String areaCode) throws IOException {

        AttractionDetailResponse detail = attractionService.getAttractionDetail(contentId, contentTypeId, areaCode);
        return ResponseEntity.ok(detail);
    }
}
