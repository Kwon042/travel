package com.example.travelProj.domain.attraction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
// API 응답 데이터를 받을 DTO
public class AttractionResponse {
    private String title;
    private String firstImage;
    private String addr;
    private String description;  // 관광지 설명을 위한 필드
    private double mapx;  // 경도 (longitude)
    private double mapy;  // 위도 (latitude)
    private Long contentId;
    private String contentTypeId;

    // 모든 필드를 사용하는 생성자
    public AttractionResponse(String title, String firstImage, String addr, String description, double mapx, double mapy, Long contentId, String contentTypeId) {
        this.title = title;
        this.firstImage = firstImage;
        this.addr = addr;
        this.description = description;
        this.mapx = mapx;
        this.mapy = mapy;
        this.contentId = contentId;
        this.contentTypeId = contentTypeId;

    }

    // 이미지 URL을 반환하는 메서드
    public String getImageUrl() {

        return firstImage;
    }

    // 관광지 이름을 반환하는 메서드
    public String getName() {
        return title;
    }

    // 관광지 설명을 반환하는 메서드 (예시로 주소를 설명으로 사용)
    public String getDescription() {
        return description != null ? description : addr;
    }
}
