package com.example.travelProj.domain.api;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
// 한국관광공사에서 제공하는 전체 json 응답을 담은 dto
public class ApiResponseDto {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        private Items items;
    }

    @Data
    public static class Items {
        private List<Item> item;
    }

    @Data
    public static class Item {
        private String addr1;
        private String addr2;
        private String areacode;
        private String title;
        private String mapx;
        private String mapy;
    }
}

