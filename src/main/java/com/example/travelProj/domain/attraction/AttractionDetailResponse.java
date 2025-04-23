package com.example.travelProj.domain.attraction;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AttractionDetailResponse {
    private String title;
    private String addr1;
    private String tel;
    private String overview;
    private String firstimage;

    // 생성자, getter, setter 추가

    public AttractionDetailResponse(JsonNode itemNode) {
        this.title = itemNode.at("/title").asText();
        this.addr1 = itemNode.at("/addr1").asText();
        this.tel = itemNode.at("/tel").asText();
        this.overview = itemNode.at("/overview").asText();
        this.firstimage = itemNode.at("/firstimage").asText();
    }
}
