package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.attraction.Attraction;
import com.example.travelProj.domain.attraction.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AttractionService {

    final private AttractionRepository attractionRepository;

    // API_KEY를 application.properties 파일에서 가져옴
    @Value("${tour.api.key}")
    private String apiKey;

    // 관광지 API 엔드포인트
    private final String API_URL = "https://api.visitkorea.or.kr/openapi/service/rest/GoTour/getTourList?serviceKey=";

//    @Autowired
//    private RestTemplate restTemplate;
//
//    public List<Attraction> getRandomAttractions() {
//        // API 호출 URL 구성
//        String url = API_URL + apiKey;
//
//        // API 호출하여 AttractionResponse 가져오기
//        AttractionResponse response = restTemplate.getForObject(url, AttractionResponse.class);
//
//        // 응답에서 관광지 목록 가져오기
//        List<AttractionResponse.AttractionItem> attractionItems = response.getItems();
//
//        // AttractionItem을 Attraction 객체로 변환
//        List<Attraction> attractions = new ArrayList<>();
//        for (AttractionResponse.AttractionItem item : attractionItems) {
//            Attraction attraction = new Attraction();
//            attraction.setName(item.getName());
//            attraction.setDescription(item.getDescription());
//            attraction.setImageUrl(item.getImageUrl());
//            attraction.setLocation(item.getLocation());
//            attractions.add(attraction);
//        }
//
//        // 랜덤으로 섞고 3개 선택하여 반환
//        Collections.shuffle(attractions);
//        return attractions.subList(0, Math.min(attractions.size(), 3));
//    }

    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 관광지가 존재하지 않습니다."));
    }

}
