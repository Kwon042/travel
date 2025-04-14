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

    private final AttractionRepository attractionRepository;

    // 키워드로 여행지 검색
    public List<Attraction> searchAttractions(String keyword) {
        return attractionRepository.findByNameContaining(keyword);
    }

    // 랜덤 여행지 가져오기
    public List<Attraction> getRandomAttractions() {
        return attractionRepository.findRandomAttractions();
    }

    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 관광지가 존재하지 않습니다."));
    }

}
