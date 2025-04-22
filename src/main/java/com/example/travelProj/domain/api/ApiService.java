package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
// 데이터 제공용 (프론트 JS 에서 호출) >  데이터 서버
public class ApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.url}")
    private String apiUrl;
    @Value("${api.key}")
    private String apiKey;

    // api 요청 전담
    public String searchAttraction(String keyword) {
        try {
            // keyword와 apiKey 둘 다 인코딩
            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());

            // URL 요청 만들기
            String requestUrl = apiUrl
                    + "?serviceKey=" + encodedApiKey
                    + "&keyword=" + encodedKeyword
                    + "&MobileOS=ETC"
                    + "&MobileApp=TestApp"
                    + "&_type=json";

            System.out.println("api request URL: " + requestUrl);  // 요청 URL 로그 출력

            // WebClient로 GET 요청
            String response = webClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return response;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "인코딩 오류 발생";
        }
    }

    // 데이터 가공 전담
    public List<AttractionResponse> parseApiResponse(String apiResponse) {
        List<AttractionResponse> attractions = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(apiResponse);
            JsonNode itemsNode = root.at("/response/body/items/item");

            if (itemsNode.isMissingNode() || itemsNode.isNull()) {
                System.out.println("관광지 검색 결과가 없습니다.");
                return attractions;
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
            e.printStackTrace();
        }
        return attractions;
    }

    // 변환
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
