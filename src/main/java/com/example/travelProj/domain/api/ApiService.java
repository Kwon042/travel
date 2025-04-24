package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionDetailResponse;
import com.example.travelProj.domain.attraction.AttractionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
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
    public List<AttractionResponse> searchAttractionByRegion(String areaCode, String contentTypeId) {
        if (areaCode == null || areaCode.isBlank()) {
            return Collections.emptyList();
        }

        // 지역 코드로 관광지 검색
        String apiResponse = fetchAttractionsByRegion(areaCode, contentTypeId);
        return parseApiResponse(apiResponse);
    }

    // 지역 코드로 관광지 목록을 요청
    private String fetchAttractionsByRegion(String areaCode, String contentTypeId) {
        try {
            logger.info("Region search request - Region code: {}", areaCode);

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
                            .queryParam("contentTypeId", contentTypeId)
                            .queryParam("numOfRows", 100)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("API response: {}", response);
            return response;

        } catch (Exception e) {
            logger.error("API request failed", e);
            return "{\"error\": \"An error occurred during the API request\"}";
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
                logger.warn("Public API error resultCode: {}", resultCode);
                return Collections.emptyList();
            }

            JsonNode itemsNode = root.at("/response/body/items/item");

            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                logger.info("No search results found");
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
            logger.error("Error while parsing the response", e);
        }

        return attractions;
    }

    // 단일 JsonNode를 AttractionResponse로 변환
    private AttractionResponse convertToAttraction(JsonNode item) {
        String title = item.path("title").asText(null);
        if (title == null || title.isBlank()) return null;

        String firstImage = item.path("firstimage").asText("");
        String addr = item.path("addr1").asText("");
        String description = item.path("description").asText("");
        double mapx = item.path("mapx").asDouble(0.0);
        double mapy = item.path("mapy").asDouble(0.0);
        Long contentId = item.path("contentid").asLong(0);

        return new AttractionResponse(title, firstImage, addr, description, mapx, mapy, contentId);
    }

    // 상세정보 - 메인
    public AttractionDetailResponse fetchDetailInfo(Long contentId) {
        try {
            JsonNode mainItemNode = fetchCommonInfo(contentId);
            if (mainItemNode == null) return null;

            JsonNode infoListNode = fetchAdditionalInfo(contentId);

            return new AttractionDetailResponse(mainItemNode, infoListNode);
        } catch (Exception e) {
            logger.error("Failed to fetch detail info", e);
            return null;
        }
    }

    // 상세정보 - 기본적인  정보만
    private JsonNode fetchCommonInfo(Long contentId) throws IOException {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/B551011/KorService1/detailCommon1")
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "TestApp")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", "12")
                        .queryParam("defaultYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(response);
        String resultCode = root.at("/response/header/resultCode").asText();
        if (!"0000".equals(resultCode)) {
            logger.warn("CommonInfo API error: {}", resultCode);
            return null;
        }

        return root.at("/response/body/items/item");
    }

    // 상세정보 - 부가적인 정보
    private JsonNode fetchAdditionalInfo(Long contentId) throws IOException {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/B551011/KorService1/detailInfo1")
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "TestApp")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", "12")
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(response);
        return root.at("/response/body/items/item");
    }

}

