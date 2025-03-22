package com.example.travelProj;

import com.example.travelProj.attraction.Attraction;
//import com.example.travelProj.attraction.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MainController {

    @GetMapping("/")
    public String main() {
        return "main";
    }
}

//    @Autowired
//    private AttractionService attractionService;
//
//    @GetMapping("/")
//    public String main(Model model) {
//        List<Attraction> attractions = attractionService.getRandomAttractions();
//        model.addAttribute("attractions", attractions);
//        return "main"; // Thymeleaf 템플릿
//    }