package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.Attraction;
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
import java.util.List;

@Service
@RequiredArgsConstructor
// 데이터 제공용 (프론트 JS 에서 호출) >  데이터 서버
public class ApiService {

    private final WebClient webClient;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.key}")
    private String apiKey;

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
            return new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "인코딩 오류 발생";
        }
    }

    public List<AttractionResponse> parseApiResponse(String apiResponse) {
        List<AttractionResponse> attractions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // JSON 응답을 파싱
            JsonNode root = mapper.readTree(apiResponse);
            JsonNode items = root.at("/response/body/items/item"); // JSON 구조 맞춰서 수정

            if (items.isMissingNode() || items.isNull()) {
                System.out.println("관광지 검색 결과가 없습니다.");
                return attractions; // 빈 리스트 반환
            }

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = item.path("title").asText();
                    String firstImage = item.path("firstimage").asText();
                    String addr = item.path("addr1").asText();
                    double mapx = item.path("mapx").asDouble();
                    double mapy = item.path("mapy").asDouble();

                    AttractionResponse attractionResponse = new AttractionResponse(title, firstImage, addr, mapx, mapy);
                    attractions.add(attractionResponse);
                }
            } else {
                // 검색결과가 1건일 경우 JSON 배열이 아닌 단일 객체 형태로 내려올 수도 있음
                String title = items.path("title").asText();
                String firstImage = items.path("firstimage").asText();
                String addr = items.path("addr1").asText();
                double mapx = items.path("mapx").asDouble();
                double mapy = items.path("mapy").asDouble();

                attractions.add(new AttractionResponse(title, firstImage, addr, mapx, mapy));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attractions;
    }


}
