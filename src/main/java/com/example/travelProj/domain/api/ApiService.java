package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8.name());
            String encodedApiKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());

            // URL 요청 만들기
            String requestUrl = apiUrl
                    + "?serviceKey=" + encodedApiKey
                    + "&keyword=" + encodedKeyword
                    + "&MobileOS=ETC"
                    + "&MobileApp=TestApp"
                    + "&_type=json";

            System.out.println("api request URL: " + requestUrl);  // 요청 URL 로그 출력

            // WebClient로 GET 요청
            return webClient.get()
                    .uri(requestUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "인코딩 오류 발생";
        }
    }



}
