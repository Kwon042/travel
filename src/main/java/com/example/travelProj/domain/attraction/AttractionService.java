package com.example.travelProj.domain.attraction;

import com.example.travelProj.domain.api.ApiService;
import com.example.travelProj.domain.api.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AttractionService {

    private final ApiService apiService;

    // 랜덤 여행지 - 비동기 처리
    public List<AttractionResponse> getRandomAttractions() {
        List<String> regionCodes = List.of("1", "2", "3", "4", "5", "6", "7", "8", "31", "32", "33", "34", "35", "36", "37", "38", "39");
        List<String> contentTypeIds = List.of("12", "14", "15", "28", "32", "38", "39");

        List<CompletableFuture<List<AttractionResponse>>> futures = new ArrayList<>();
        Random random = new Random();

        for (String regionCode : regionCodes) {
            // 각 지역마다 랜덤한 관광지 타입 선택
            String contentTypeId = contentTypeIds.get(random.nextInt(contentTypeIds.size()));
            // 비동기 호출 추가
            CompletableFuture<List<AttractionResponse>> future = CompletableFuture.supplyAsync(() -> apiService.searchAttractionByRegion(regionCode, contentTypeId));
            futures.add(future);
        }

        // 모든 API 요청을 병렬로 처리하고, 결과를 합침
        List<AttractionResponse> allAttractions = futures.stream()
                .map(CompletableFuture::join)  // 각 비동기 요청의 결과를 기다림
                .flatMap(List::stream)  // 결과를 모두 합침
                .collect(Collectors.toList());

        // 섞고 일부만 반환
        Collections.shuffle(allAttractions);
        return allAttractions.stream()
                .limit(12)  // 항상 12개 고정
                .toList();
    }

    // 지도 페이지에서 지역에 맞는 관광지 최대 100개 가져오기
    public List<AttractionResponse> getAttractionsForMap(String regionName) {
        String regionCode = RegionMapper.getAreaCode(regionName);

        List<String> contentTypeIds = List.of("12", "14", "15", "28", "32", "38", "39");
        // 랜덤으로 하나 선택
        String contentTypeId = contentTypeIds.get(new Random().nextInt(contentTypeIds.size()));

        List<AttractionResponse> attractions = apiService.searchAttractionByRegion(regionCode, contentTypeId);

        return attractions.stream()
                .limit(100)
                .toList();
    }

    public AttractionDetailResponse getAttractionDetail(Long contentId, String contentTypeId, String areaCode) {
        AttractionDetailResponse detail = apiService.fetchDetailInfo(contentId, contentTypeId, areaCode);

        if (detail != null && (detail.getFirstimage() == null || detail.getFirstimage().trim().isEmpty())) {
            // 👉 firstimage가 없을 경우 지역기반 API에서 해당 ID의 이미지를 찾아 사용
            List<AttractionResponse> candidates = apiService.searchAttractionByRegion(areaCode, contentTypeId);
            // contentId가 일치하는 Attraction 찾기
            AttractionResponse match = candidates.stream()
                    .filter(attraction -> attraction.getContentId().equals(contentId))
                    .findFirst()
                    .orElse(null);

            if (match != null && match.getFirstimage() != null && !match.getFirstimage().trim().isEmpty()) {
                detail.setFirstimage(match.getFirstimage());
            } else {
                // 마지막으로 기본 이미지 지정
                detail.setFirstimage("/images/no-image.png");
            }
        }

        return detail;
    }



}
