package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attraction")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @GetMapping("/search")
    public List<AttractionResponse> searchAttraction(@RequestParam String keyword) {
        String apiResponse = apiService.searchAttraction(keyword);
        return apiService.parseApiResponse(apiResponse);
    }




}
