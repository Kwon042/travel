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
// 검색 > 상세보기용 dto
public class AttractionDetailResponse {
    private Long contentId;
    private String contentTypeId;
    private String title;
    private String addr1;
    private String tel;
    private String overview;
    private String firstimage;
    private List<Info> infoList;
    private String areaCode;

    public AttractionDetailResponse(JsonNode mainItemNode, JsonNode itemsArrayNode, String areaCode) {
        System.out.println("mainItemNode: " + mainItemNode.toPrettyString());

        this.contentId = mainItemNode.path("contentid").asLong();
        this.contentTypeId = mainItemNode.path("contenttypeid").asText("");
        this.areaCode = areaCode;
        this.title = mainItemNode.path("title").asText("");
        this.addr1 = mainItemNode.path("addr1").asText("");
        this.tel = mainItemNode.path("tel").asText("");
        this.overview = mainItemNode.path("overview").asText("");
        if (mainItemNode.has("firstimage")) {
            this.firstimage = mainItemNode.get("firstimage").asText("");
        }
        this.infoList = new ArrayList<>();
        if (itemsArrayNode != null && itemsArrayNode.isArray()) {
            for (JsonNode infoNode : itemsArrayNode) {
                Info info = new Info(
                        infoNode.path("infoname").asText(),
                        infoNode.path("infotext").asText()
                );
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

    // static 메서드 'of' 추가
    public static AttractionDetailResponse of(JsonNode common, JsonNode intro, JsonNode infoList, String areaCode) {
        return new AttractionDetailResponse(common, infoList, areaCode);
    }
}
