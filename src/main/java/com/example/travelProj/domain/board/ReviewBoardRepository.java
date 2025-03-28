package com.example.travelProj.domain.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewBoardRepository extends JpaRepository<ReviewBoard, Long> {
    List<ReviewBoard> findByRegion_RegionName(String regionName);
    Optional<ReviewBoard> findByRegion_RegionNameAndTitle(String regionName, String title);
}
