package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.api.SimpleAttractionDto;
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

    // 랜덤 여행지 가져오기
    public List<AttractionResponse> getRandomAttractions() {
        String apiResponse = apiService.searchAttraction("전국");
        List<AttractionResponse> attractions = apiService.parseApiResponse(apiResponse);

        Collections.shuffle(attractions);  // 순서 섞기
        return attractions.stream()
                .limit(new Random().nextInt(3) + 3)  // 3~5개 랜덤으로 자르기
                .toList();
    }

    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 관광지가 존재하지 않습니다."));
    }
}
