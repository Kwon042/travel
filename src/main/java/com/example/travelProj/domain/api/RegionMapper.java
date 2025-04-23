package com.example.travelProj.domain.api;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RegionMapper {

    private static final Map<String, String> regionToCodeMap = new HashMap<>();

    static {
        regionToCodeMap.put("전국", "");
        regionToCodeMap.put("서울", "1");
        regionToCodeMap.put("부산", "6");
        regionToCodeMap.put("대구", "4");
        regionToCodeMap.put("인천", "2");
        regionToCodeMap.put("광주", "5");
        regionToCodeMap.put("대전", "3");
        regionToCodeMap.put("울산", "7");
        regionToCodeMap.put("세종", "8");
        regionToCodeMap.put("경기", "31");
        regionToCodeMap.put("강원", "32");
        regionToCodeMap.put("충북", "33");
        regionToCodeMap.put("충남", "34");
        regionToCodeMap.put("경북", "35");
        regionToCodeMap.put("경남", "36");
        regionToCodeMap.put("전북", "37");
        regionToCodeMap.put("전남", "38");
        regionToCodeMap.put("제주", "39");
    }

    public static String getAreaCode(String regionName) {
        return regionToCodeMap.getOrDefault(regionName, "");
    }
}
