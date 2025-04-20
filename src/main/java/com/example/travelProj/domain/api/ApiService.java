package com.example.travelProj.domain.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.kor.service-key}")
    private String serviceKey;

    public List<SimpleAttractionDto> fetchTouristAttractions(String keyword) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("http://apis.data.go.kr/B551011/KorService1/searchKeyword1")
                    .queryParam("serviceKey", serviceKey) // 인코딩 금지
                    .queryParam("keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8))
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TestApp")
                    .queryParam("_type", "json")
                    .build(false) // 인코딩 X
                    .toUri();

            String body = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("공공API 요청 실패: statusCode = {}", response.statusCode());
                        return Mono.error(new RuntimeException("공공API 호출 실패"));
                    })
                    .bodyToMono(String.class)
                    .block();

            if (!body.trim().startsWith("{")) {
                log.error("공공API 응답이 JSON이 아님: {}", body);
                throw new RuntimeException("공공API 응답 형식 오류");
            }

            return parseToSimpleDtoList(body);

        } catch (Exception e) {
            log.error("공공API 호출 중 예외 발생", e);
            return Collections.emptyList();
        }
    }

    private List<SimpleAttractionDto> parseToSimpleDtoList(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        JsonNode items = root.at("/response/body/items/item");
        List<SimpleAttractionDto> result = new ArrayList<>();

        if (items.isArray()) {
            for (JsonNode item : items) {
                result.add(SimpleAttractionDto.builder()
                        .title(item.path("title").asText())
                        .addr1(item.path("addr1").asText(null))
                        .firstimage(item.path("firstimage").asText(null))
                        .mapx(item.path("mapx").asDouble(0.0))
                        .mapy(item.path("mapy").asDouble(0.0))
                        .contentid(item.path("contentid").asText(null))
                        .contenttypeid(item.path("contenttypeid").asText(null))
                        .build());
            }
        }

        return result;
    }
}
