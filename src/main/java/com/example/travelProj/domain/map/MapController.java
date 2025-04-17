package com.example.travelProj.domain.map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
// 웹 페이지에서 지도를 보여줄 때 필요
public class MapController {

    @GetMapping("/map")
    public String showMapPage() {
        return "map";
    }
}
