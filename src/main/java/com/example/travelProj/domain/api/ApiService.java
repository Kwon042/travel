package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionDetailResponse;
import com.example.travelProj.domain.attraction.AttractionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "regionSearchCache", key = "#areaCode + '_' + #contentTypeId")
    public List<AttractionResponse> searchAttractionByRegion(String areaCode, String contentTypeId) {
        // System.out.println("Requesting API with areaCode: " + areaCode + " and contentTypeId: " + contentTypeId);

        if (areaCode == null || areaCode.isBlank()) {
            return Collections.emptyList();
        }
        // 지역 코드로 관광지 검색
        String apiResponse = fetchAttractionsByRegion(areaCode, contentTypeId);

//        System.out.println(apiResponse);
//        System.out.println("areaCode: " + areaCode);
//        System.out.println("contentTypeId: " + contentTypeId);

        return parseApiResponse(apiResponse, areaCode);
    }

    // 지역 코드로 관광지 목록을 요청
    @Cacheable(value = "detailInfoCache", key = "#contentId")
    private String fetchAttractionsByRegion(String areaCode, String contentTypeId) {
        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("apis.data.go.kr")
                            .path("/B551011/KorService2/areaBasedList2")
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

            JsonNode root = objectMapper.readTree(response);
            logger.debug("API response: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
            return response;

        } catch (Exception e) {
            logger.error("API request failed", e);
            return "{\"error\": \"An error occurred during the API request\"}";
        }
    }

    // 응답 JSON 파싱 후 AttractionResponse 리스트로 변환
    private List<AttractionResponse> parseApiResponse(String apiResponse, String areaCode) {
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
                    AttractionResponse attraction = convertToAttraction(item, areaCode);
                    if (attraction != null) attractions.add(attraction);
                }
            } else if (itemsNode.isObject()) {
                AttractionResponse attraction = convertToAttraction(itemsNode, areaCode);
                if (attraction != null) attractions.add(attraction);
            }

        } catch (Exception e) {
            logger.error("Error while parsing the response", e);
        }

        return attractions;
    }

    // 단일 JsonNode를 AttractionResponse로 변환
    private AttractionResponse convertToAttraction(JsonNode item, String areaCode) {
        String title = item.path("title").asText(null);
        if (title == null || title.isBlank()) return null;

        String firstimage = item.path("firstimage").asText("");
        // logger.debug("firstimage: {}", firstimage);  // 이미지 값 확인

        String addr = item.path("addr1").asText("");
        String description = item.path("description").asText("");
        double mapx = item.path("mapx").asDouble(0.0);
        double mapy = item.path("mapy").asDouble(0.0);
        Long contentId = item.path("contentid").asLong(0);
        String contentTypeId = item.path("contenttypeid").asText("");

        return new AttractionResponse(title, firstimage, addr, description, mapx, mapy, areaCode, contentId, contentTypeId);
    }

    // 상세정보 - 메인
    public AttractionDetailResponse fetchDetailInfo(Long contentId, String contentTypeId, String areaCode) {
        try {
            JsonNode mainItemNode = fetchCommonInfo(contentId, contentTypeId);
            if (mainItemNode == null || mainItemNode.isMissingNode()) return null;

            // 배열 형태로 오는 경우 첫 번째 요소만 사용
            if (mainItemNode.isArray() && mainItemNode.size() > 0) {
                mainItemNode = mainItemNode.get(0);
            }

             logger.debug("Main Detail JSON:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mainItemNode));

            JsonNode infoListNode = fetchAdditionalInfo(contentId, contentTypeId);

             logger.debug("Additional Info JSON:\n{}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(infoListNode));

            return new AttractionDetailResponse(mainItemNode, infoListNode, areaCode);
        } catch (Exception e) {
            logger.error("Failed to fetch detail info", e);
            return null;
        }
    }

    // 상세정보 - 기본적인  정보만
    public JsonNode fetchCommonInfo(Long contentId, String contentTypeId) throws IOException {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/B551011/KorService2/detailCommon2")
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "TestApp")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("defaultYN", "Y")
                        .queryParam("overviewYN", "Y")
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        // logger.debug("Received response: {}", response);  // 응답 로그 추가


        JsonNode root = objectMapper.readTree(response);
        String resultCode = root.at("/response/header/resultCode").asText();
        if (!"0000".equals(resultCode)) {
            logger.warn("CommonInfo API error: {}", resultCode);
            return null;
        }

        return root.at("/response/body/items/item");
    }

    // 상세정보 - 부가적인 정보
    public JsonNode fetchAdditionalInfo(Long contentId, String contentTypeId) throws IOException {
        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("apis.data.go.kr")
                        .path("/B551011/KorService2/detailInfo2")
                        .queryParam("serviceKey", apiKey)
                        .queryParam("MobileOS", "ETC")
                        .queryParam("MobileApp", "TestApp")
                        .queryParam("contentId", contentId)
                        .queryParam("contentTypeId", contentTypeId)
                        .queryParam("_type", "json")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(response);
        return root.at("/response/body/items/item");
    }

}

