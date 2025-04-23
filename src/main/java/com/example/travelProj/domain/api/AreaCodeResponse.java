package com.example.travelProj.domain.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 응답에서 우리가 필요 없는 필드 무시하도록 해줌
public class AreaCodeResponse {

    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JsonProperty("name") //JSON 필드명이 자바 필드명과 다를 경우 명시적으로 매핑
        private String name;

        @JsonProperty("code")
        private int code;

        @JsonProperty("rnum")
        private int rnum;

        @JsonProperty("areaCode")
        private int areaCode;
    }
}
