package com.example.travelProj.global.converter;

import com.example.travelProj.domain.region.Region;
import com.example.travelProj.domain.region.RegionRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

// @Component: 자동으로 Bean 등록됨
@Component
public class StringToRegionConverter implements Converter<String, Region> {
    private final RegionRepository regionRepository;

    public StringToRegionConverter(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Override
    public Region convert(String source) {
        return regionRepository.findByRegionName(source)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다."));
    }
}