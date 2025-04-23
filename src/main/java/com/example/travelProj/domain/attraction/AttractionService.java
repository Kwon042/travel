package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.api.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class AttractionService {

    private final AttractionRepository attractionRepository;
    private final ApiService apiService;

    public List<AttractionResponse> getRandomAttractions() {
        // 모든 지역 코드 리스트
        List<String> regionCodes = List.of("1", "2", "3", "4", "5", "6", "7", "8", "31", "32", "33", "34", "35", "36", "37", "38", "39");

        // 모든 지역 코드에 대해 관광지 데이터를 가져옴
        List<AttractionResponse> allAttractions = new ArrayList<>();
        for (String regionCode : regionCodes) {
            List<AttractionResponse> attractions = apiService.searchAttractionByRegion(regionCode);
            allAttractions.addAll(attractions);
        }

        // 랜덤으로 3~5개 선택
        Collections.shuffle(allAttractions);
        return allAttractions.stream()
                .limit(new Random().nextInt(2) + 5)
                .toList();
    }

    // 지도 페이지에서 지역에 맞는 관광지 최대 100개 가져오기
    public List<AttractionResponse> getAttractionsForMap(String regionName) {
        String regionCode = RegionMapper.getAreaCode(regionName);
        List<AttractionResponse> attractions = apiService.searchAttractionByRegion(regionCode);

        return attractions.stream()
                .limit(100)
                .toList();
    }

    // 특정 ID의 관광지 정보 조회
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 관광지가 존재하지 않습니다."));
    }
}
