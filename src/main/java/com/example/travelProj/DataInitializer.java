package com.example.travelProj;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;

@RequiredArgsConstructor
@Component
// 프로젝트 매 실행시 지역 데이터 삽입
public class DataInitializer implements CommandLineRunner {

    private final RegionRepository regionRepository;

    @Override
    public void run(String... args) throws Exception {
        // 초기화할 지역 리스트
        List<Region> regions = Arrays.asList(
                new Region("전체"),
                new Region("서울"),
                new Region("대전"),
                new Region("대구"),
                new Region("광주"),
                new Region("부산"),
                new Region("울산"),
                new Region("세종"),
                new Region("경기"),
                new Region("인천"),
                new Region("강원"),
                new Region("충북"),
                new Region("충남"),
                new Region("경북"),
                new Region("경남"),
                new Region("전북"),
                new Region("전남"),
                new Region("제주")
        );

        // 데이터가 비어 있는 경우만 삽입
        if (regionRepository.count() == 0) {
            regionRepository.saveAll(regions);
            System.out.println("초기 지역 데이터가 저장되었습니다.");
        } else {
            System.out.println("지역 데이터가 이미 존재합니다.");
        }
    }
}
