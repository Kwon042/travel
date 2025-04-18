package com.example.travelProj.domain.attraction;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private double mapx;  // 경도 (longitude)
    private double mapy;  // 위도 (latitude)

    // 모든 필드를 사용하는 생성자
    public AttractionResponse(String title, String firstImage, String addr, double mapx, double mapy) {
        this.title = title;
        this.firstImage = firstImage;
        this.addr = addr;
        this.mapx = mapx;
        this.mapy = mapy;
    }
}
