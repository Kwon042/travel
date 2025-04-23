package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
// 데이터 제공용 (프론트 JS 에서 호출) >  데이터 서버
public class ApiService {
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.url}")
    private String apiUrl;
    @Value("${api.key}")
    private String apiKey;

    // 지역명을 기반으로 관광지 검색
    public List<AttractionResponse> searchAttractionByRegion(String regionCode) {
        if (regionCode == null || regionCode.isBlank()) {
            return Collections.emptyList();
        }

        // 지역 코드로 관광지 검색
        String apiResponse = fetchAttractionsByRegion(regionCode);
        return parseApiResponse(apiResponse);
    }

    // 지역 코드로 관광지 목록을 요청
    private String fetchAttractionsByRegion(String areaCode) {
        try {
            logger.info("지역 검색 요청 - 지역 코드: {}", areaCode);

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService1/areaBasedList1")
                            .queryParam("serviceKey", apiKey)
                            .queryParam("areaCode", areaCode)
                            .queryParam("MobileOS", "ETC")
                            .queryParam("MobileApp", "TestApp")
                            .queryParam("_type", "json")
                            .queryParam("numOfRows", 50)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("API 응답: {}", response);
            return response;

        } catch (Exception e) {
            logger.error("API 요청 실패", e);
            return "{\"error\": \"API 요청 중 오류 발생\"}";
        }
    }

    // 응답 JSON 파싱 후 AttractionResponse 리스트로 변환
    private List<AttractionResponse> parseApiResponse(String apiResponse) {
        List<AttractionResponse> attractions = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(apiResponse);

            // API 오류 확인
            String resultCode = root.at("/response/header/resultCode").asText();
            if (!"0000".equals(resultCode)) {
                logger.warn("공공 API 오류 resultCode: {}", resultCode);
                return Collections.emptyList();
            }

            JsonNode itemsNode = root.at("/response/body/items/item");

            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                logger.info("검색 결과 없음");
                return Collections.emptyList();
            }

            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    AttractionResponse attraction = convertToAttraction(item);
                    if (attraction != null) attractions.add(attraction);
                }
            } else if (itemsNode.isObject()) {
                AttractionResponse attraction = convertToAttraction(itemsNode);
                if (attraction != null) attractions.add(attraction);
            }

        } catch (Exception e) {
            logger.error("응답 파싱 중 오류", e);
        }

        return attractions;
    }

    // 단일 JsonNode를 AttractionResponse로 변환
    private AttractionResponse convertToAttraction(JsonNode item) {
        String title = item.path("title").asText(null);
        if (title == null || title.isBlank()) return null;

        String firstImage = item.path("firstimage").asText("");
        String addr = item.path("addr1").asText("");
        double mapx = item.path("mapx").asDouble(0.0);
        double mapy = item.path("mapy").asDouble(0.0);

        return new AttractionResponse(title, firstImage, addr, mapx, mapy);
    }
}

