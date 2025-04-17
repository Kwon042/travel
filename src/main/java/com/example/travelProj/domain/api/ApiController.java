package com.example.travelProj.domain.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attraction")
@RequiredArgsConstructor
public class ApiController {

    private final ApiService apiService;

    @GetMapping("/search")
    public String searchAttraction(@RequestParam String keyword) {

        return apiService.searchAttraction(keyword);
    }




}
