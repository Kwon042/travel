package com.example.travelProj.domain.region;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String regionName;

    // 기본 생성자
    public Region() {}

    // 지역 이름을 받는 생성자
    public Region(String regionName) {
        this.regionName = regionName;
    }

}
