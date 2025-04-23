package com.example.travelProj.domain.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.jsoup.internal.StringUtil.isNumeric;

@Service
@RequiredArgsConstructor
public class RegionCodeService {
    private static final Logger logger = LoggerFactory.getLogger(RegionCodeService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apiKey;

    // 캐시
    private final Map<String, String> sigunguToAreaCodeCache = new ConcurrentHashMap<>();

    // 지역 키워드를 기반으로 지역 코드를 반환 > 시/도 우선, 없으면 시/군/구 API 조회
    public String findAreaCodeByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;

        String normalized = keyword.trim().toLowerCase();
        logger.info("입력된 지역명: {}", normalized);

        // 숫자만 있는 경우, 이미 지역 코드로 간주
        if (isNumeric(normalized)) {
            logger.info("입력된 지역명이 숫자만 포함되어 있어 지역 코드로 간주: {}", normalized);
            return normalized;
        }

        // 1. RegionMapper를 이용한 시/도 코드 반환
        String mappedCode = RegionMapper.getAreaCode(normalized);
        if (mappedCode != null) {
            logger.info("지역명 '{}'에 대한 시/도 코드 반환: {}", normalized, mappedCode);
            return mappedCode;
        } else {
            logger.warn("지역명 '{}'에 대한 시/도 코드 찾기 실패", normalized);
        }

        // 2. 시/도에 없으면 시/군/구 단위로 API 조회
        String apiCode = findAreaCodeBySigunguFromApi(normalized);
        if (apiCode != null) {
            logger.info("지역명 '{}'에 대한 시/군/구 코드 반환: {}", normalized, apiCode);
            return apiCode;
        } else {
            logger.warn("지역명 '{}'에 대한 시/군/구 코드 찾기 실패", normalized);
        }

        return null;  // 두 방법 모두 실패한 경우 null 반환
    }

    // 숫자인지 확인하는 메서드
    private boolean isNumeric(String str) {
        return str != null && str.matches("\\d+"); // \\d+: 하나 이상의 숫자로 이루어진 문자열
    }

    // 시/군/구 단위 키워드로 지역 코드를 API에서 조회
    private String findAreaCodeBySigunguFromApi(String keyword) {
        // 캐시 확인
        if (sigunguToAreaCodeCache.containsKey(keyword)) {
            return sigunguToAreaCodeCache.get(keyword);
        }

        // 전국 areaCode 순회 (1~8, 31~39)
        List<String> areaCodes = List.of(
                "1", "2", "3", "4", "5", "6", "7", "8",
                "31", "32", "33", "34", "35", "36", "37", "38", "39"
        );

        for (String areaCode : areaCodes) {
            try {
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
                                .queryParam("numOfRows", 50)
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                AreaCodeResponse areaCodeResponse = objectMapper.readValue(response, AreaCodeResponse.class);
                List<AreaCodeResponse.Item> items = areaCodeResponse.getResponse()
                        .getBody()
                        .getItems()
                        .getItem();

                for (AreaCodeResponse.Item item : items) {
                    String normalizedItemName = item.getName().trim().toLowerCase();
                    if (normalizedItemName.contains(keyword)) {
                        sigunguToAreaCodeCache.put(keyword, areaCode);
                        return areaCode;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 로깅 대체 가능
            }
        }

        return null;
    }
}
