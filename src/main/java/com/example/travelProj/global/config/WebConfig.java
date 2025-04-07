package com.example.travelProj.global.config;

import com.example.travelProj.global.converter.StringToRegionConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringToRegionConverter stringToRegionConverter;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(); // 외부 API 호출을 위한 RestTemplate
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 매핑 (CSS, JS, 이미지 등)
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
        // 업로드된 이미지 경로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
        // 게시판 이미지 전용 매핑 (ex. /board/12/abc.jpg 요청 가능)
        registry.addResourceHandler("/board/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/board/");

    }

}