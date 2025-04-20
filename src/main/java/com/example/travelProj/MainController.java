package com.example.travelProj;

import com.example.travelProj.domain.api.SimpleAttractionDto;
import com.example.travelProj.domain.attraction.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final AttractionService attractionService;

    @GetMapping("/")
    public String showMainPage(Model model) {
        // 랜덤 여행지 3~5개를 가져와서 모델에 추가
        List<SimpleAttractionDto> randomAttractions = attractionService.getRandomAttractions();
        model.addAttribute("randomAttractions", randomAttractions);
        return "main";
    }
}

