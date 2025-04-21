package com.example.travelProj.domain.api;

import com.example.travelProj.domain.attraction.AttractionResponse;
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
    public ResponseEntity<List<AttractionResponse>> searchAttractions(@RequestParam String keyword) {
        String rawResponse = apiService.searchAttraction(keyword);
        List<AttractionResponse> parsedList = apiService.parseApiResponse(rawResponse);

        System.out.println();
        return ResponseEntity.ok(parsedList);
    }

}
