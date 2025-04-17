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
            // 응답이 실패한 경우
            if (response.contains("SERVICE_ERROR")) {
                return "API 호출 실패: " + response;  // API 호출 실패 시 에러 메시지 반환
            }

            return response;  // 정상 응답 반환

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "인코딩 오류 발생";
        }
    }

    public List<AttractionResponse> parseApiResponse(String apiResponse) {
        List<AttractionResponse> attractions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(apiResponse);
            JsonNode items = root.at("/response/body/items/item"); // JSON 구조 맞춰서 수정

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String title = item.path("title").asText();         // 제목
                    String firstImage = item.path("firstimage").asText(); // 이미지 URL
                    String addr = item.path("addr1").asText();           // 주소

                    // AttractionResponse 객체 생성
                    AttractionResponse attractionResponse = new AttractionResponse(title, firstImage, addr);
                    attractions.add(attractionResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attractions;
    }


}
