package com.example.travelProj.domain.attraction;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
// 관광지 > 상세보기용 dto
public class AttractionDetailResponse {
    private String title;
    private String addr1;
    private String tel;
    private String overview;
    private String firstimage;
    private List<Info> infoList; // 새로운 필드 추가

    public AttractionDetailResponse(JsonNode mainItemNode, JsonNode itemsArrayNode) {
        this.title = mainItemNode.at("title").asText();
        this.addr1 = mainItemNode.at("addr1").asText();
        this.tel = mainItemNode.at("tel").asText();
        this.overview = mainItemNode.at("overview").asText();
        this.firstimage = mainItemNode.at("firstimage").asText();

        this.infoList = new ArrayList<>();
        if (itemsArrayNode.isArray()) {
            for (JsonNode infoNode : itemsArrayNode) {
                Info info = new Info(infoNode.path("infoname").asText(),
                        infoNode.path("infotext").asText());
                this.infoList.add(info);
            }
        }
    }

    // Info 클래스 추가
    public static class Info {
        private String infoName;
        private String infoText;

        public Info(String infoName, String infoText) {
            this.infoName = infoName;
            this.infoText = infoText;
        }

        public String getInfoName() {
            return infoName;
        }

        public String getInfoText() {
            return infoText;
        }
    }
}
