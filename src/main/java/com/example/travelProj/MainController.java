package com.example.travelProj;

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

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final UserService userService;
    private final AttractionService attractionService;

    @GetMapping("/")
    public String showMainPage(@AuthenticationPrincipal SiteUser user, Model model, HttpSession session) {
        SiteUser updatedUser = userService.findUserById(user.getId());
        session.setAttribute("user", updatedUser);
        model.addAttribute("user", updatedUser);

        // 랜덤 여행지를 가져와서 모델에 추가
//        List<AttractionResponse> randomAttractions = attractionService.getRandomAttractions();
//        model.addAttribute("randomAttractions", randomAttractions);
        return "main";
    }
}

