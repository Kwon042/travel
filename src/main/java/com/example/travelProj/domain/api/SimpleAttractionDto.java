package com.example.travelProj.domain.api;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// 클라이언트에 전달할 여행지 정보만 담은 dto
public class SimpleAttractionDto {
    private String title;
    private String addr1;
    private String firstimage;
    private double mapx;
    private double mapy;
    private String contentid;
    private String contenttypeid;
}
