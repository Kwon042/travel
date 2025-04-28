package com.example.travelProj.domain.map;

import com.example.travelProj.domain.attraction.AttractionResponse;
import com.example.travelProj.domain.attraction.AttractionService;
import com.example.travelProj.domain.user.SiteUser;
import com.example.travelProj.domain.user.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@RequiredArgsConstructor
@Controller
// 웹 페이지에서 지도를 보여줄 때 필요
public class MapController {

    private final AttractionService attractionService;
    private final UserService userService;

    @GetMapping("/map")
    public String showMapPage(@AuthenticationPrincipal SiteUser user, Model model, HttpSession session) {
        SiteUser updatedUser = userService.findUserById(user.getId());
        session.setAttribute("user", updatedUser);
        model.addAttribute("user", updatedUser);

        return "map";
    }

    // /api/attractions 요청 시 JSON 데이터 반환 (js에서) > 마커 찍기 위해
    @GetMapping("/api/attractions")
    @ResponseBody
    public List<AttractionResponse> getAttractionsForMap(@RequestParam String region) {
        // AttractionService를 사용하여 관광지 데이터를 조회
        return attractionService.getAttractionsForMap(region);
    }
}
