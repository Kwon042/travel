package com.example.travelProj.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    // Java 객체와 JSON 데이터 간의 변환
    // JSON 데이터를 다루는 데 필요한 기능을 제공 > Spring에서 API 요청/응답을 처리할 때 자동으로 사용
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}