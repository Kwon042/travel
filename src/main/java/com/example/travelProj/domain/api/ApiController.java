package com.example.travelProj.domain.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<SimpleAttractionDto>> searchAttractions(@RequestParam String keyword) {
        List<SimpleAttractionDto> result = apiService.fetchTouristAttractions(keyword);
        return ResponseEntity.ok(result);
    }

}
