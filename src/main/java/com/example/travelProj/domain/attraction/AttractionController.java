//package com.example.travelProj.domain.attraction;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//@RestController
//@RequestMapping("/attractions")
//public class AttractionController {
//
//    private final AttractionService attractionService;
//
//    // 여행지 검색 엔드포인트
//    @GetMapping("/search")
//    public List<Attraction> searchAttractions(@RequestParam String keyword) {
//        return attractionService.searchAttractions(keyword);
//    }
//
//    // 랜덤 여행지 가져오기 엔드포인트
//    @GetMapping("/random")
//    public List<Attraction> getRandomAttractions() {
//        return attractionService.getRandomAttractions();
//    }
//}
