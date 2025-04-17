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

    // 필드를 사용하여 DTO 객체를 생성하는 생성자
    public AttractionResponse(String title, String firstImage, String addr) {
        this.title = title;
        this.firstImage = firstImage;
        this.addr = addr;
    }
}
