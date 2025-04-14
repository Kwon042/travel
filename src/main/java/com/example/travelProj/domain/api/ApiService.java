package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final WebClient webClient;
    private final AttractionRepository attractionRepository;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.key}")
    private String apiKey;

    public String searchAttraction(String keyword) {
        // 요청 URL 조립
        String requestUrl = apiUrl + "?serviceKey=" + apiKey + "&keyword=" + keyword + "&MobileOS=ETC&MobileApp=TestApp&_type=json";

        // WebClient로 GET 요청
        return webClient.get()
                .uri(requestUrl)
                .retrieve()
                .bodyToMono(String.class)  // JSON을 String으로 받음
                .block();  // 동기적으로 결과 반환
    }


}
