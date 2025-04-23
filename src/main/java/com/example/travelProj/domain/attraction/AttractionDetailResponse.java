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


    public AttractionDetailResponse(JsonNode itemNode) {
        this.title = itemNode.at("/title").asText();
        this.addr1 = itemNode.at("/addr1").asText();
        this.tel = itemNode.at("/tel").asText();
        this.overview = itemNode.at("/overview").asText();
        this.firstimage = itemNode.at("/firstimage").asText();

        // info 데이터를 파싱하여 리스트로 추가
        this.infoList = new ArrayList<>();
        JsonNode itemsNode = itemNode.path("items").path("item");
        if (itemsNode.isArray()) {
            for (JsonNode infoNode : itemsNode) {
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
