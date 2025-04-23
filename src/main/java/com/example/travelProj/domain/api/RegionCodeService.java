package com.example.travelProj.domain.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RegionCodeService {

    private final WebClient webClient;

    @Value("${api.key}")
    private String apiKey;

    // 캐시: "천안" -> "34"
    private final Map<String, String> sigunguToAreaCodeCache = new ConcurrentHashMap<>();

    public String findAreaCodeBySigungu(String keyword) {

        // 캐시 먼저 확인
        if (sigunguToAreaCodeCache.containsKey(keyword)) {
            return sigunguToAreaCodeCache.get(keyword);
        }

        // 모든 areaCode 시도
        List<String> areaCodes = List.of("1", "2", "3", "4", "5", "6", "7", "8",
                "31", "32", "33", "34", "35", "36", "37", "38", "39");

        for (String areaCode : areaCodes) {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService1/areaCode1")
                            .queryParam("serviceKey", apiKey)
                            .queryParam("_type", "json")
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "AppTest")
                            .queryParam("areaCode", areaCode)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && response.contains(keyword)) {
                // 찾았으면 캐시에 저장하고 리턴
                sigunguToAreaCodeCache.put(keyword, areaCode);
                return areaCode;
            }
        }

        return null;
    }
}
