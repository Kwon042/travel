//package com.example.travelProj.global.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.ViewResolver;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.view.InternalResourceViewResolver;
//
//@Configuration
//@EnableWebMvc // Spring MVC 활성화
//public class WebConfig {
//
//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate(); // 외부 API 호출을 위한 RestTemplate
//    }
//
//    @Bean
//    public ViewResolver viewResolver() {
//        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//        resolver.setPrefix("/WEB-INF/views/"); // JSP 파일이 있는 경로
//        resolver.setSuffix(".jsp"); // JSP 파일 확장자
//        return resolver;
//    }
//}