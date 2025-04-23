package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.api.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final ApiService apiService;

    // 메인 페이지에서 랜덤 여행지 가져오기 (전국 기준)
    public List<AttractionResponse> getRandomAttractions() {
        return getRandomAttractionsByRegion("전국");  // 기본 "전국" 기준으로 랜덤 여행지 3~5개 가져오기
    }

    // 지역 기반 랜덤 여행지 가져오기
    public List<AttractionResponse> getRandomAttractionsByRegion(String regionName) {
        String regionCode = RegionMapper.getAreaCode(regionName);  // "전국" → "" 자동 매핑
        List<AttractionResponse> attractions = apiService.searchAttractionByRegion(regionCode);

        Collections.shuffle(attractions);
        return attractions.stream()
                .limit(new Random().nextInt(3) + 3) // 3~5개 랜덤
                .toList();
    }

    // 지도 페이지에서 지역에 맞는 관광지 최대 50개 가져오기
    public List<AttractionResponse> getAttractionsForMap(String regionName) {
        String regionCode = RegionMapper.getAreaCode(regionName);
        List<AttractionResponse> attractions = apiService.searchAttractionByRegion(regionCode);

        return attractions.stream()
                .limit(50)
                .toList();
    }

    // 특정 ID의 관광지 정보 조회
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 관광지가 존재하지 않습니다."));
    }
}
